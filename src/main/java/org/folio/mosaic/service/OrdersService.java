package org.folio.mosaic.service;

import org.folio.mosaic.client.OrdersClient;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrdersService {

  private final OrdersClient ordersClient;

  public String createOrder(CompositePurchaseOrder compositePurchaseOrder) {
    log.info("createOrder:: Creating mosaic order with number: {}", compositePurchaseOrder.getPoNumber());
    val createdOrder = ordersClient.createOrder(compositePurchaseOrder);
    return createdOrder.getCompositePoLines().getFirst().getPoLineNumber();
  }

}
