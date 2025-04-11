package org.folio.mosaic.client;

import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("orders")
public interface OrdersClient {

  @PostMapping(value = "/orders")
  CompositePurchaseOrder createOrder(@RequestBody CompositePurchaseOrder poLine);

}
