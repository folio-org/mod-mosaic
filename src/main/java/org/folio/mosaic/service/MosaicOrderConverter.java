package org.folio.mosaic.service;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
   * @param request The order request
   * @param template The order template to use (can be null)
   * @return A CompositePurchaseOrder object ready to be sent to FOLIO
   */
  public CompositePurchaseOrder convertToCompositePurchaseOrder(MosaicOrder request, OrderTemplate template) {
    CompositePurchaseOrder order = new CompositePurchaseOrder();

    // Apply template values first if available
    if (template != null) {
      applyTemplateToOrder(order, template);
    }

    // Apply override values from the request
//    applyOverrides(order, request);

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
   * Applies template values to the purchase order.
   * This uses the structure of the OrderTemplate to set appropriate values on the purchase order.
   *
   */
  private void applyTemplateToOrder(CompositePurchaseOrder order, OrderTemplate template) {
    order.setTemplate(template.getId());

    Map<String, Object> templateProps = template.getAdditionalProperties();
    // 1. Order-level fields
    if (templateProps.get("orderType") != null) {
      order.setOrderType(CompositePurchaseOrder.OrderType.valueOf(templateProps.get("orderType").toString()));
    }

    if (templateProps.get("acqUnitIds") != null) {
      order.setAcqUnitIds(
        ((List<String>) templateProps.get("acqUnitIds")).stream()
              .map(String::valueOf)
              .collect(Collectors.toList()));
    }

    if (templateProps.get("billTo") != null) {
      order.setBillTo(templateProps.get("billTo").toString());
    }
    if (templateProps.get("shipTo") != null) {
      order.setShipTo(templateProps.get("shipTo").toString());
    }
    if (templateProps.get("vendor") != null) {
      order.setVendor(templateProps.get("vendor").toString());
    }
    if (templateProps.get("notes") != null) {
      order.setNotes((List<String>) templateProps.get("notes"));
    }

    // 2. Order line template fields
    if (order.getPoLines() == null || order.getPoLines().isEmpty()) {
      order.setPoLines(new ArrayList<>(singletonList(new PoLine())));
    }

    PoLine poLine = order.getPoLines().getFirst();

    if (templateProps.get("orderFormat") != null) {
      poLine.setOrderFormat(OrderFormat.valueOf(templateProps.get("orderFormat").toString()));
    }

    if (templateProps.get("acquisitionMethod") != null) {
      poLine.setAcquisitionMethod(templateProps.get("acquisitionMethod").toString());
    }

    if (templateProps.get("receivingNote") != null) {
      var details = poLine.getDetails() != null ? poLine.getDetails() : new Details();
      details.setReceivingNote(templateProps.get("receivingNote").toString());
      poLine.setDetails(details);
    }

    if (templateProps.get("materialType") != null) {
      var physical = poLine.getPhysical() != null ? poLine.getPhysical() : new Physical();
      physical.setMaterialType(templateProps.get("materialType").toString());
      poLine.setPhysical(physical);
    }

    if (templateProps.get("materialSupplier") != null) {
      var physical = poLine.getPhysical() != null ? poLine.getPhysical() : new Physical();
      physical.setMaterialSupplier(templateProps.get("materialSupplier").toString());
      poLine.setPhysical(physical);
    }

    if (templateProps.get("locations") != null)  {
      poLine.setLocations(new ArrayList<>((List<Location>)templateProps.get("locations")));
    }

    if (templateProps.get("fundDistribution") != null) {
      poLine.setFundDistribution(new ArrayList<>((List<FundDistribution>)templateProps.get("fundDistribution")));
    }

    if (templateProps.get("accessProvider") != null) {
      var eresource = poLine.getEresource() != null ? poLine.getEresource() : new Eresource();
      eresource.setAccessProvider(templateProps.get("accessProvider").toString());
      poLine.setEresource(eresource);
    }

    if (templateProps.get("customFields") != null) {
      order.setCustomFields((CustomFields) templateProps.get("customFields"));
    }
  }

  /**
   * Applies overrides from the request to the purchase order.
   * This will set values based on the request, overriding any template values.
   *
   * @param order The purchase order to modify
   * @param mosaicOrder The request containing override values
  */
  private void applyOverrides(CompositePurchaseOrder order, MosaicOrder mosaicOrder) {
    if (mosaicOrder.getVendor() != null) {
      order.setVendor(mosaicOrder.getVendor());
    }
    if (mosaicOrder.getBillTo() != null) {
      order.setBillTo(mosaicOrder.getBillTo());
    }
    if (mosaicOrder.getShipTo() != null) {
      order.setShipTo(mosaicOrder.getShipTo());
    }

    if (mosaicOrder.getAcquisitionUnitId() != null) {
      // TODO: double check if it is come as array
      order.setAcqUnitIds(Collections.singletonList(mosaicOrder.getAcquisitionUnitId()));
    }

//    if (mosaicOrder.getCustomFields() != null) {
//      order.setCustomFields(mosaicOrder.getCustomFields());
//    }

    var poLine = new PoLine();

    if (isNotBlank(mosaicOrder.getTitle())) {
      poLine.setTitleOrPackage(mosaicOrder.getTitle());
    }

    if (mosaicOrder.getListUnitPrice() != null || mosaicOrder.getListUnitPriceElectronic() != null) {
      Cost cost = poLine.getCost() != null ? poLine.getCost() : new Cost();

      // Determine if electronic or physical based on order format
      MosaicOrder.Format format = mosaicOrder.getFormat();

      if (format == MosaicOrder.Format.ELECTRONIC || mosaicOrder.getListUnitPriceElectronic() != null) {
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

      // Set currency
      if (isNotBlank(mosaicOrder.getCurrency())) {
        cost.setCurrency(mosaicOrder.getCurrency());
      }

      poLine.setCost(cost);
    }

    if (isNotBlank(mosaicOrder.getAuthor())) {
      Contributor contributor = new Contributor();
      contributor.setContributor(mosaicOrder.getAuthor());
//      contributor.setContributorNameTypeId();

      List<Contributor> contributors = singletonList(contributor);
      poLine.setContributors(contributors);
    }

    if (isNotBlank(mosaicOrder.getPublicationDate())) {
      poLine.setPublicationDate(mosaicOrder.getPublicationDate());
    }

    if (isNotBlank(mosaicOrder.getEdition())) {
      poLine.setEdition(mosaicOrder.getEdition());
    }

    if (mosaicOrder.getProductId() != null) {
      var productIdentifier = new ProductIdentifier().withProductId(mosaicOrder.getProductId());
      var details = new Details()
        .withProductIds(singletonList(productIdentifier));
//      List<String> productIds = details.getProductIds() != null ? details.getProductIds() : new ArrayList<>();

//      for (MosaicOrderRequest.ProductIdentifier isbn : request.getIsbn()) {
//        ProductId productId = new ProductId();
//        productId.setProductId(isbn.getProductId());
//        productId.setProductIdType(isbn.getProductIdType());
//        productId.setQualifier(isbn.getQualifier());
//        productIds.add(productId);
//      }
      poLine.setDetails(details);
    }

    if (isNotBlank(mosaicOrder.getReceivingNote())) {
      Details details = new Details();
      details.setReceivingNote(mosaicOrder.getReceivingNote());
      poLine.setDetails(details);
    }

    if (isNotBlank(mosaicOrder.getVendor())) {
      VendorDetail vendorDetail = new VendorDetail();
      List<ReferenceNumberItem> referenceNumbers = new ArrayList<>();

      for (var mosaicRefNumber : mosaicOrder.getReferenceNumbers()) {
        var referenceNumber = new ReferenceNumberItem();
        referenceNumber.setRefNumber(mosaicRefNumber.getRefNumber());
        referenceNumber.setRefNumberType(ReferenceNumberItem.RefNumberType.valueOf(mosaicRefNumber.getRefNumberType().toString()));
        referenceNumbers.add(referenceNumber);
      }

      vendorDetail.setReferenceNumbers(referenceNumbers);
      poLine.setVendorDetail(vendorDetail);
    }

    if (isNotBlank(mosaicOrder.getUserLimit())) {
      var eresource = new Eresource().withUserLimit(mosaicOrder.getUserLimit());
      poLine.setEresource(eresource);
    }

    if (isNotBlank(mosaicOrder.getRequesterName())) {
      poLine.setRequester(mosaicOrder.getRequesterName());
    }

    if (isNotBlank(mosaicOrder.getSelectorName())) {
      poLine.setSelector(mosaicOrder.getSelectorName());
    }

    if (isNotBlank(mosaicOrder.getPoLineNote())) {
      order.setNotes(singletonList(mosaicOrder.getPoLineNote()));
    }

    if (isNotBlank(mosaicOrder.getPoLineDescription())) {
      poLine.setPoLineDescription(mosaicOrder.getPoLineDescription());
    }

    if (isNotBlank(mosaicOrder.getRenewalNote())) {
      poLine.setRenewalNote(mosaicOrder.getRenewalNote());
    }

    if (isNotBlank(mosaicOrder.getLocationId())) {
      List<Location> locations = new ArrayList<>();

//      for (UUID locationId : mosaicOrder.getLocationIds()) {
//        Location location = new Location();
//        location.setLocationId(locationId.toString());
//
//        // Set quantities based on format
//        if (mosaicOrder.getOrderFormat() == MosaicOrdermosaicOrder.OrderFormat.ELECTRONIC) {
//          location.setQuantityElectronic(1);
//          location.setQuantityPhysical(0);
//          location.setQuantity(1);
//        } else if (mosaicOrder.getOrderFormat() == MosaicOrdermosaicOrder.OrderFormat.PE_MIX) {
//          location.setQuantityElectronic(1);
//          location.setQuantityPhysical(1);
//          location.setQuantity(2);
//        } else {
//          location.setQuantityElectronic(0);
//          location.setQuantityPhysical(1);
//          location.setQuantity(1);
//        }
//
//        locations.add(location);
//      }

      poLine.setLocations(locations);
    }

    if (isNotBlank(mosaicOrder.getFundId())) {
      List<FundDistribution> fundDistributions = new ArrayList<>();

//      for (MosaicOrdermosaicOrder.FundDistribution fund : mosaicOrder.getFunds()) {
//        FundDistribution distribution = new FundDistribution();
//        distribution.setFundId(fund.getFundId().toString());
//        distribution.setDistributionType(fund.getDistributionType());
//        distribution.setValue(fund.getValue().doubleValue());
//
//        if (fund.getExpenseClassId() != null) {
//          distribution.setExpenseClassId(fund.getExpenseClassId().toString());
//        }
//
//        fundDistributions.add(distribution);
//      }

      poLine.setFundDistribution(fundDistributions);
    }

    if (mosaicOrder.getMaterialTypeId() != null) {
      Physical physical = poLine.getPhysical() != null ? poLine.getPhysical() : new Physical();
      physical.setMaterialType(mosaicOrder.getMaterialTypeId());
      poLine.setPhysical(physical);
    }

    if (isNotBlank(mosaicOrder.getAcquisitionUnitId())) {
      order.setAcqUnitIds(singletonList(mosaicOrder.getAcquisitionUnitId()));
    }

    if (mosaicOrder.getMaterialTypeId() != null) {
      Physical physical =  new Physical();
      physical.setMaterialSupplier(mosaicOrder.getMaterialTypeId());
      poLine.setPhysical(physical);
    }

    if (mosaicOrder.getAccessProvider() != null) {
      var eresource = new Eresource();
      eresource.setAccessProvider(mosaicOrder.getAccessProvider());
      poLine.setEresource(eresource);
    }
  }
}
