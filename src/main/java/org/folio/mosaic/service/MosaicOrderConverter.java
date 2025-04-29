package org.folio.mosaic.service;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.Contributor;
import org.folio.rest.acq.model.orders.Cost;
import org.folio.rest.acq.model.orders.CustomFields;
import org.folio.rest.acq.model.orders.Details;
import org.folio.rest.acq.model.orders.Eresource;
import org.folio.rest.acq.model.orders.FundDistribution;
import org.folio.rest.acq.model.orders.Location;
import org.folio.rest.acq.model.orders.OrderFormat;
import org.folio.rest.acq.model.orders.OrderTemplate;
import org.folio.rest.acq.model.orders.Physical;
import org.folio.rest.acq.model.orders.PoLine;
import org.folio.rest.acq.model.orders.ProductIdentifier;
import org.folio.rest.acq.model.orders.ReferenceNumberItem;
import org.folio.rest.acq.model.orders.VendorDetail;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MosaicOrderConverter {

  /**
   * Converts a MosaicOrderRequest to a CompositePurchaseOrder
   * 1. Applies template values if available
   * 2. Override values from the request
   *
   * @param mosaicOrder  The mosaic order from request
   * @param template The order template to use (can be null)
   * @return A CompositePurchaseOrder object ready to be sent to FOLIO
   */
  public CompositePurchaseOrder convertToCompositePurchaseOrder(MosaicOrder mosaicOrder, CompositePurchaseOrder template) {
    log.debug("convertToCompositePurchaseOrder:: Converting mosaicOrder: {} to compositePurchaseOrder", mosaicOrder.getTitle());
    var order = new CompositePurchaseOrder();

    if (template == null) {
      log.warn("convertToCompositePurchaseOrder:: No template found for mosaicOrder: {}", mosaicOrder.getTitle());
      throw new ResourceNotFoundException(OrderTemplate.class);
    }

    log.info("convertToCompositePurchaseOrder:: Using template: {}", template.getId());
    order = createOrderFromTemplate(template);

    log.info("convertToCompositePurchaseOrder:: Applying overrides from mosaicOrder");
    applyOverrides(order, mosaicOrder);

    return order;
  }

  private CompositePurchaseOrder createOrderFromTemplate(CompositePurchaseOrder template) {
    log.debug("createOrderFromTemplate:: Creating order from template: {}", template.getId());
    var templatePoLines = template.getPoLines() != null
      ? template.getPoLines()
      : new ArrayList<>(singletonList(new PoLine()));
    var orderType = template.getOrderType() != null
      ? template.getOrderType()
      : CompositePurchaseOrder.OrderType.ONE_TIME;

    return new CompositePurchaseOrder()
      .withId(UUID.randomUUID().toString())
      .withTemplate(template.getId())
      .withOrderType(orderType)
      .withAcqUnitIds(template.getAcqUnitIds())
      .withBillTo(template.getBillTo())
      .withShipTo(template.getShipTo())
      .withVendor(template.getVendor())
      .withNotes(template.getNotes())
      .withCustomFields(template.getCustomFields())
      .withPoLines(templatePoLines);
  }

  /**
   * Applies overrides from the request to the purchase order.
   * This will set values based on the request, overriding any template values.
   *
   * @param order       The purchase order to modify
   * @param mosaicOrder The request containing override values
   */
  private void applyOverrides(CompositePurchaseOrder order, MosaicOrder mosaicOrder) {
    if (mosaicOrder.getId() != null) {
      order.setId(mosaicOrder.getId());
    }

    if (mosaicOrder.getVendor() != null) {
      order.setVendor(mosaicOrder.getVendor());
    }
    if (mosaicOrder.getBillTo() != null) {
      order.setBillTo(mosaicOrder.getBillTo());
    }
    if (mosaicOrder.getShipTo() != null) {
      order.setShipTo(mosaicOrder.getShipTo());
    }

    if (CollectionUtils.isNotEmpty(mosaicOrder.getAcqUnitIds())) {
      order.setAcqUnitIds(mosaicOrder.getAcqUnitIds());
    }

    if (mosaicOrder.getCustomFields() != null) {
      var convertedCustomFields = new CustomFields();
      mosaicOrder.getCustomFields().getAdditionalProperties().forEach(convertedCustomFields::withAdditionalProperty);
      order.setCustomFields(convertedCustomFields);
    }

    var poLine = new PoLine();

    if (isNotBlank(mosaicOrder.getTitle())) {
      poLine.setTitleOrPackage(mosaicOrder.getTitle());
    }

    updatePoLineCost(mosaicOrder, poLine);

    updatePoLineContributors(mosaicOrder, poLine);

    if (isNotBlank(mosaicOrder.getPublicationDate())) {
      poLine.setPublicationDate(mosaicOrder.getPublicationDate());
    }

    if (isNotBlank(mosaicOrder.getEdition())) {
      poLine.setEdition(mosaicOrder.getEdition());
    }

    updatePoLineDetails(mosaicOrder, poLine);

    updatePoLineVendor(mosaicOrder, poLine);

    if (isNotBlank(mosaicOrder.getUserLimit())) {
      var eresource = poLine.getEresource() != null ? poLine.getEresource() : new Eresource();
      eresource.setUserLimit(mosaicOrder.getUserLimit());
      poLine.setEresource(eresource);
    }

    if (isNotBlank(mosaicOrder.getRequesterName())) {
      poLine.setRequester(mosaicOrder.getRequesterName());
    }

    if (isNotBlank(mosaicOrder.getSelectorName())) {
      poLine.setSelector(mosaicOrder.getSelectorName());
    }

    if (isNotBlank(mosaicOrder.getOrderNote())) {
      order.setNotes(singletonList(mosaicOrder.getOrderNote()));
    }

    if (isNotBlank(mosaicOrder.getPoLineDescription())) {
      poLine.setPoLineDescription(mosaicOrder.getPoLineDescription());
    }

    if (isNotBlank(mosaicOrder.getRenewalNote())) {
      poLine.setRenewalNote(mosaicOrder.getRenewalNote());
    }

    updatePoLineLocations(mosaicOrder, poLine);

    updatePoLineFunds(mosaicOrder, poLine);

    if (mosaicOrder.getMaterialTypeId() != null) {
      var physical = poLine.getPhysical() != null ? poLine.getPhysical() : new Physical();
      physical.setMaterialType(mosaicOrder.getMaterialTypeId());
      physical.setMaterialSupplier(mosaicOrder.getMaterialSupplier());
      poLine.setPhysical(physical);
    }

    if (mosaicOrder.getAccessProvider() != null) {
      var eresource = poLine.getEresource() != null ? poLine.getEresource() : new Eresource();
      eresource.setAccessProvider(mosaicOrder.getAccessProvider());
      poLine.setEresource(eresource);
    }

    order.setPoLines(List.of(poLine));
  }

  private void updatePoLineContributors(MosaicOrder mosaicOrder, PoLine poLine) {
    if (CollectionUtils.isNotEmpty(mosaicOrder.getContributors())) {
      var convertedContributors = mosaicOrder.getContributors()
        .stream()
        .map(mosaicContributor ->
          new Contributor()
            .withContributor(mosaicContributor.getContributor())
            .withContributorNameTypeId(mosaicContributor.getContributorNameTypeId()))
        .toList();
      poLine.setContributors(convertedContributors);
    }
  }

  private void updatePoLineCost(MosaicOrder mosaicOrder, PoLine poLine) {
    if (mosaicOrder.getListUnitPrice() != null || mosaicOrder.getListUnitPriceElectronic() != null) {
      Cost cost = poLine.getCost() != null ? poLine.getCost() : new Cost();

      MosaicOrder.Format format = mosaicOrder.getFormat();

      if (format == MosaicOrder.Format.ELECTRONIC && mosaicOrder.getListUnitPriceElectronic() != null) {
        cost.setListUnitPriceElectronic(mosaicOrder.getListUnitPriceElectronic());
        cost.setQuantityElectronic(mosaicOrder.getQuantityElectronic());
        cost.setQuantityPhysical(0);
        poLine.setOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);
      } else if (format == MosaicOrder.Format.P_E_MIX) {
        cost.setListUnitPrice(mosaicOrder.getListUnitPrice());
        cost.setListUnitPriceElectronic(mosaicOrder.getListUnitPriceElectronic());
        cost.setQuantityPhysical(mosaicOrder.getQuantityPhysical());
        cost.setQuantityElectronic(mosaicOrder.getQuantityElectronic());
        poLine.setOrderFormat(OrderFormat.P_E_MIX);
      } else {
        cost.setListUnitPrice(mosaicOrder.getListUnitPrice());
        cost.setQuantityPhysical(mosaicOrder.getQuantityPhysical());
        cost.setQuantityElectronic(0);
        poLine.setOrderFormat(OrderFormat.PHYSICAL_RESOURCE);
      }

      if (isNotBlank(mosaicOrder.getCurrency())) {
        cost.setCurrency(mosaicOrder.getCurrency());
      }

      poLine.setCost(cost);
    }
  }

  private void updatePoLineDetails(MosaicOrder mosaicOrder, PoLine poLine) {
    if (ObjectUtils.isNotEmpty(mosaicOrder.getDetails())) {
      var mosaicDetails = mosaicOrder.getDetails();

      var convertedDetails = new Details()
        .withIsAcknowledged(mosaicDetails.getIsAcknowledged())
        .withIsBinderyActive(mosaicDetails.getIsBinderyActive())
        .withReceivingNote(mosaicDetails.getReceivingNote())
        .withSubscriptionFrom(mosaicDetails.getSubscriptionFrom())
        .withSubscriptionTo(mosaicDetails.getSubscriptionTo())
        .withSubscriptionInterval(mosaicDetails.getSubscriptionInterval());

      var mosaicProductIds = mosaicDetails.getProductIds();
      if (CollectionUtils.isNotEmpty(mosaicProductIds)) {
        convertedDetails.setProductIds(mosaicProductIds
          .stream()
          .map(mosaicProductId -> new ProductIdentifier()
            .withProductId(mosaicProductId.getProductId())
            .withProductIdType((mosaicProductId.getProductIdType())))
          .toList());
      }

      poLine.setDetails(convertedDetails);
    }
  }

  private void updatePoLineVendor(MosaicOrder mosaicOrder, PoLine poLine) {
    if (isNotBlank(mosaicOrder.getVendor())) {
      VendorDetail vendorDetail = new VendorDetail();
      List<ReferenceNumberItem> referenceNumbers = new ArrayList<>();

      for (var mosaicRefNumber : mosaicOrder.getReferenceNumbers()) {
        var referenceNumber = new ReferenceNumberItem();
        referenceNumber.setRefNumber(mosaicRefNumber.getRefNumber());
        referenceNumber.setRefNumberType(ReferenceNumberItem.RefNumberType.fromValue(mosaicRefNumber.getRefNumberType().toString()));
        referenceNumbers.add(referenceNumber);
      }

      vendorDetail.setReferenceNumbers(referenceNumbers);
      poLine.setVendorDetail(vendorDetail);
    }
  }

  private void updatePoLineLocations(MosaicOrder mosaicOrder, PoLine poLine) {
    if (CollectionUtils.isNotEmpty(mosaicOrder.getLocations())) {
      var convertedLocations = mosaicOrder.getLocations()
        .stream()
        .map(mosaicLocation ->
          new Location()
            .withLocationId(mosaicLocation.getLocationId())
            .withQuantity(mosaicLocation.getQuantity())
            .withHoldingId(mosaicLocation.getHoldingId())
            .withTenantId(mosaicLocation.getTenantId())
            .withQuantityPhysical(mosaicLocation.getQuantityPhysical())
            .withQuantityElectronic(mosaicLocation.getQuantityElectronic()))
        .toList();
      poLine.setLocations(convertedLocations);
    }
  }

  private void updatePoLineFunds(MosaicOrder mosaicOrder, PoLine poLine) {
    if (CollectionUtils.isNotEmpty(mosaicOrder.getFundDistribution())) {
      var convertedFunds = mosaicOrder.getFundDistribution()
        .stream()
        .map(mosaicFund ->
          new FundDistribution()
            .withFundId(mosaicFund.getFundId())
            .withDistributionType(FundDistribution.DistributionType.fromValue(mosaicFund.getDistributionType().toString()))
            .withValue(mosaicFund.getValue()))
        .toList();
      poLine.setFundDistribution(convertedFunds);
    }
  }
}
