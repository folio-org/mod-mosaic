package org.folio.mosaic.service;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.CustomFields;
import org.folio.rest.acq.model.orders.PoLine;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class MosaicOrderConverter {

  private final MosaicPoLineConverter mosaicPoLineConverter;

  /**
   * Converts a MosaicOrderRequest to a CompositePurchaseOrder
   * 1. Applies template values if available
   * 2. Override values from the request
   *
   * @param mosaicOrder  The mosaic order from request
   * @param templatePair The order template pair to use
   * @return A CompositePurchaseOrder object ready to be sent to FOLIO
   */
  public CompositePurchaseOrder convertToCompositePurchaseOrder(MosaicOrder mosaicOrder, Pair<CompositePurchaseOrder, PoLine> templatePair) {
    log.debug("convertToCompositePurchaseOrder:: Converting mosaicOrder: {} to compositePurchaseOrder", mosaicOrder.getTitle());

    log.info("convertToCompositePurchaseOrder:: Using template: {}", templatePair.getKey().getId());
    var order = createOrderFromTemplatePair(templatePair);

    log.info("convertToCompositePurchaseOrder:: Applying overrides from mosaicOrder");
    applyOverrides(order, mosaicOrder);

    return order;
  }

  /**
   * Creates a CompositePurchaseOrder with an included PoLine from a template
   *
   * @param templatePair Holding order and poLine template fields
   * @return A CompositePurchaseOrder object with an included PoLine
   */
  private CompositePurchaseOrder createOrderFromTemplatePair(Pair<CompositePurchaseOrder, PoLine> templatePair) {
    log.debug("createOrderFromTemplatePair:: Creating order from template: {}", templatePair.getKey().getId());

    var orderTemplate = templatePair.getKey();
    var orderType = orderTemplate.getOrderType() != null
      ? orderTemplate.getOrderType() : CompositePurchaseOrder.OrderType.ONE_TIME;
    var poLine = mosaicPoLineConverter.createPoLineFromTemplate(templatePair.getValue());

    return new CompositePurchaseOrder()
      .withId(UUID.randomUUID().toString())
      .withApproved(orderTemplate.getApproved())
      .withApprovedById(orderTemplate.getApprovedById())
      .withApprovalDate(orderTemplate.getApprovalDate())
      .withAssignedTo(orderTemplate.getAssignedTo())
      .withBillTo(orderTemplate.getBillTo())
      .withCloseReason(orderTemplate.getCloseReason())
      .withDateOrdered(orderTemplate.getDateOrdered())
      .withManualPo(orderTemplate.getManualPo())
      .withNotes(orderTemplate.getNotes())
      .withPoNumberPrefix(orderTemplate.getPoNumberPrefix())
      .withPoNumberSuffix(orderTemplate.getPoNumberSuffix())
      .withOrderType(orderType)
      .withReEncumber(orderTemplate.getReEncumber())
      .withOngoing(orderTemplate.getOngoing())
      .withShipTo(orderTemplate.getShipTo())
      .withTemplate(orderTemplate.getId())
      .withTotalCredited(orderTemplate.getTotalCredited())
      .withTotalEstimatedPrice(orderTemplate.getTotalEstimatedPrice())
      .withTotalEncumbered(orderTemplate.getTotalEncumbered())
      .withTotalExpended(orderTemplate.getTotalExpended())
      .withTotalItems(orderTemplate.getTotalItems())
      .withVendor(orderTemplate.getVendor())
      .withWorkflowStatus(orderTemplate.getWorkflowStatus())
      .withPoLines(new ArrayList<>(singletonList(poLine)))
      .withAcqUnitIds(orderTemplate.getAcqUnitIds())
      .withNextPolNumber(orderTemplate.getNextPolNumber())
      .withTags(orderTemplate.getTags())
      .withCustomFields(orderTemplate.getCustomFields());
  }

  /**
   * Applies overrides from the request to the purchase order.
   * This will set values based on the request, overriding any template values.
   *
   * @param order       The purchase order to modify
   * @param mosaicOrder The request containing override values
   */
  public void applyOverrides(CompositePurchaseOrder order, MosaicOrder mosaicOrder) {
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

    mosaicPoLineConverter.applyOverridesToPoLine(order, mosaicOrder);
  }
}
