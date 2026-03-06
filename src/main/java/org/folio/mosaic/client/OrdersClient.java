package org.folio.mosaic.client;

import java.io.InputStream;
import java.util.Optional;

import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.OrderTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("orders")
public interface OrdersClient {

  @PostExchange(value = "/composite-orders")
  CompositePurchaseOrder createOrder(@RequestBody CompositePurchaseOrder poLine);

  @GetExchange(value = "/order-templates/{templateId}")
  Optional<InputStream> getOrderTemplateAsResponse(@PathVariable String templateId);

  @PostExchange("/order-templates")
  void createOrderTemplate(OrderTemplate template);
}
