package org.folio.mosaic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.Contributor;
import org.folio.rest.acq.model.orders.Cost;
import org.folio.rest.acq.model.orders.Details;
import org.folio.rest.acq.model.orders.Eresource;
import org.folio.rest.acq.model.orders.FundDistribution;
import org.folio.rest.acq.model.orders.Location;
import org.folio.rest.acq.model.orders.OrderFormat;
import org.folio.rest.acq.model.orders.Physical;
import org.folio.rest.acq.model.orders.PoLine;
import org.folio.rest.acq.model.orders.ProductIdentifier;
import org.folio.rest.acq.model.orders.ReferenceNumberItem;
import org.folio.rest.acq.model.orders.VendorDetail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.folio.rest.acq.model.mosaic.MosaicOrder.Format.ELECTRONIC;

@Log4j2
@Service
@RequiredArgsConstructor
public class MosaicPoLineConverter {

  /**
   * Creates a PoLine from a template
   *
   * @param poLineTemplate Holding poLine template fields
   * @return A PoLine object
   */
  public PoLine createPoLineFromTemplate(PoLine poLineTemplate) {
    return new PoLine()
      .withId(UUID.randomUUID().toString())
      .withEdition(poLineTemplate.getEdition())
      .withCheckinItems(poLineTemplate.getCheckinItems())
      .withAgreementId(poLineTemplate.getAgreementId())
      .withAcquisitionMethod(poLineTemplate.getAcquisitionMethod())
      .withAutomaticExport(poLineTemplate.getAutomaticExport())
      .withCancellationRestriction(poLineTemplate.getCancellationRestriction())
      .withCancellationRestrictionNote(poLineTemplate.getCancellationRestrictionNote())
      .withClaims(poLineTemplate.getClaims())
      .withClaimingActive(poLineTemplate.getClaimingActive())
      .withClaimingInterval(poLineTemplate.getClaimingInterval())
      .withCollection(poLineTemplate.getCollection())
      .withContributors(poLineTemplate.getContributors())
      .withCost(poLineTemplate.getCost())
      .withDescription(poLineTemplate.getDescription())
      .withDetails(poLineTemplate.getDetails())
      .withDonor(poLineTemplate.getDonor())
      .withDonorOrganizationIds(poLineTemplate.getDonorOrganizationIds())
      .withEresource(poLineTemplate.getEresource())
      .withFundDistribution(poLineTemplate.getFundDistribution())
      .withInstanceId(poLineTemplate.getInstanceId())
      .withIsPackage(poLineTemplate.getIsPackage())
      .withLocations(poLineTemplate.getLocations())
      .withSearchLocationIds(poLineTemplate.getSearchLocationIds())
      .withLastEDIExportDate(poLineTemplate.getLastEDIExportDate())
      .withOrderFormat(poLineTemplate.getOrderFormat())
      .withPackagePoLineId(poLineTemplate.getPackagePoLineId())
      .withPaymentStatus(poLineTemplate.getPaymentStatus())
      .withPhysical(poLineTemplate.getPhysical())
      .withPoLineDescription(poLineTemplate.getPoLineDescription())
      .withPublicationDate(poLineTemplate.getPublicationDate())
      .withPublisher(poLineTemplate.getPublisher())
      .withReceiptDate(poLineTemplate.getReceiptDate())
      .withReceiptStatus(poLineTemplate.getReceiptStatus())
      .withRenewalNote(poLineTemplate.getRenewalNote())
      .withRequester(poLineTemplate.getRequester())
      .withRush(poLineTemplate.getRush())
      .withSelector(poLineTemplate.getSelector())
      .withSource(PoLine.Source.API)
      .withTags(poLineTemplate.getTags())
      .withTitleOrPackage(poLineTemplate.getTitleOrPackage())
      .withVendorDetail(poLineTemplate.getVendorDetail())
      .withCustomFields(poLineTemplate.getCustomFields());
  }

  /**
   * Applies overrides from the request to the poLine.
   * This will set values based on the request, overriding any template values.
   *
   * @param order       The purchase order to modify
   * @param mosaicOrder The request containing override values
   */
  public void applyOverridesToPoLine(CompositePurchaseOrder order, MosaicOrder mosaicOrder) {
    var poLine = order.getPoLines().getFirst();
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
    updatePoLineUserLimits(mosaicOrder, poLine);

    if (isNotBlank(mosaicOrder.getRequesterName())) {
      poLine.setRequester(mosaicOrder.getRequesterName());
    }
    if (isNotBlank(mosaicOrder.getSelectorName())) {
      poLine.setSelector(mosaicOrder.getSelectorName());
    }
    if (CollectionUtils.isNotEmpty(mosaicOrder.getNotes())) {
      order.setNotes(mosaicOrder.getNotes());
    }
    if (isNotBlank(mosaicOrder.getPoLineDescription())) {
      poLine.setPoLineDescription(mosaicOrder.getPoLineDescription());
    }
    if (isNotBlank(mosaicOrder.getRenewalNote())) {
      poLine.setRenewalNote(mosaicOrder.getRenewalNote());
    }
    updatePoLineLocations(mosaicOrder, poLine);
    updatePoLineFunds(mosaicOrder, poLine);
    updatePoLineMaterialTypeId(mosaicOrder, poLine);
    updatePoLineAccessProvider(mosaicOrder, poLine);

    if (isNotBlank(mosaicOrder.getAcquisitionMethod())) {
      poLine.setAcquisitionMethod(mosaicOrder.getAcquisitionMethod());
    }

    order.setPoLines(List.of(poLine));
  }

  private void updatePoLineUserLimits(MosaicOrder mosaicOrder, PoLine poLine) {
    if (isBlank(mosaicOrder.getUserLimit())) {
      return;
    }
    var eresource = poLine.getEresource() != null ? poLine.getEresource() : new Eresource();
    eresource.setUserLimit(mosaicOrder.getUserLimit());
    poLine.setEresource(eresource);
  }

  private void updatePoLineMaterialTypeId(MosaicOrder mosaicOrder, PoLine poLine) {
    if (isBlank(mosaicOrder.getMaterialTypeId())) {
      return;
    }
    var physical = poLine.getPhysical() != null ? poLine.getPhysical() : new Physical();
    physical.setMaterialType(mosaicOrder.getMaterialTypeId());
    physical.setMaterialSupplier(mosaicOrder.getMaterialSupplier());
    poLine.setPhysical(physical);
  }

  private void updatePoLineAccessProvider(MosaicOrder mosaicOrder, PoLine poLine) {
    if (isBlank(mosaicOrder.getAccessProvider())) {
      return;
    }
    var eresource = poLine.getEresource() != null ? poLine.getEresource() : new Eresource();
    eresource.setAccessProvider(mosaicOrder.getAccessProvider());
    poLine.setEresource(eresource);
  }

  private void updatePoLineContributors(MosaicOrder mosaicOrder, PoLine poLine) {
    if (CollectionUtils.isEmpty(mosaicOrder.getContributors())) {
      return;
    }
    var convertedContributors = mosaicOrder.getContributors()
      .stream()
      .map(mosaicContributor ->
        new Contributor()
          .withContributor(mosaicContributor.getContributor())
          .withContributorNameTypeId(mosaicContributor.getContributorNameTypeId()))
      .toList();
    poLine.setContributors(convertedContributors);
  }

  private void updatePoLineCost(MosaicOrder mosaicOrder, PoLine poLine) {
    if (mosaicOrder.getListUnitPrice() == null && mosaicOrder.getListUnitPriceElectronic() == null) {
      return;
    }
    var cost = poLine.getCost() != null ? poLine.getCost() : new Cost();
    var format = mosaicOrder.getFormat();

    if (format == ELECTRONIC && mosaicOrder.getListUnitPriceElectronic() != null) {
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

  private void updatePoLineDetails(MosaicOrder mosaicOrder, PoLine poLine) {
    if (ObjectUtils.isEmpty(mosaicOrder.getDetails())) {
      return;
    }
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
      convertedDetails.setProductIds(mosaicProductIds.stream()
        .map(mosaicProductId -> new ProductIdentifier()
          .withProductId(mosaicProductId.getProductId())
          .withProductIdType((mosaicProductId.getProductIdType())))
        .toList());
    }

    poLine.setDetails(convertedDetails);
  }

  private void updatePoLineVendor(MosaicOrder mosaicOrder, PoLine poLine) {
    if (isBlank(mosaicOrder.getVendor())) {
      return;
    }
    var vendorDetail = new VendorDetail();
    var referenceNumbers = new ArrayList<ReferenceNumberItem>();

    for (var mosaicRefNumber : mosaicOrder.getReferenceNumbers()) {
      var referenceNumber = new ReferenceNumberItem();
      referenceNumber.setRefNumber(mosaicRefNumber.getRefNumber());
      referenceNumber.setRefNumberType(ReferenceNumberItem.RefNumberType.fromValue(mosaicRefNumber.getRefNumberType().toString()));
      referenceNumbers.add(referenceNumber);
    }

    vendorDetail.setReferenceNumbers(referenceNumbers);
    poLine.setVendorDetail(vendorDetail);
  }

  private void updatePoLineLocations(MosaicOrder mosaicOrder, PoLine poLine) {
    if (CollectionUtils.isEmpty(mosaicOrder.getLocations())) {
      return;
    }
    var convertedLocations = mosaicOrder.getLocations().stream()
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

  private void updatePoLineFunds(MosaicOrder mosaicOrder, PoLine poLine) {
    if (CollectionUtils.isEmpty(mosaicOrder.getFundDistribution())) {
      return;
    }
    var convertedFunds = mosaicOrder.getFundDistribution().stream()
      .map(mosaicFund ->
        new FundDistribution()
          .withFundId(mosaicFund.getFundId())
          .withDistributionType(FundDistribution.DistributionType.fromValue(mosaicFund.getDistributionType().toString()))
          .withValue(mosaicFund.getValue()))
      .toList();
    poLine.setFundDistribution(convertedFunds);
  }
}
