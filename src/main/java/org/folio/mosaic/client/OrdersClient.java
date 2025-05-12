package org.folio.mosaic.client;

import feign.Response;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("orders")
public interface OrdersClient {

  @PostMapping(value = "/composite-orders")
  CompositePurchaseOrder createOrder(@RequestBody CompositePurchaseOrder poLine);

  @GetMapping(value = "/order-templates/{templateId}")
  Response getOrderTemplateAsResponse(@PathVariable String templateId);
}
