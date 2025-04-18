package org.folio.mosaic.service;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.mosaic.client.OrdersClient;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrdersService {

  private final OrdersClient ordersClient;
  private final ConfigurationService configurationService;

  public String createOrder(UUID templateId, CompositePurchaseOrder compositePurchaseOrder) {
    log.info("createOrder:: Creating mosaic order with number: {}", compositePurchaseOrder.getPoNumber());

    var orderTemplateId = templateId != null
      ? templateId.toString()
      : configurationService.getConfiguration().getDefaultTemplateId();

    var orderTemplate = ordersClient.getOrderTemplateById(orderTemplateId);
    // TODO: Merge the order template with the composite purchase order

    var createdOrder = ordersClient.createOrder(compositePurchaseOrder);
    return createdOrder.getCompositePoLines().getFirst().getPoLineNumber();
  }

}
