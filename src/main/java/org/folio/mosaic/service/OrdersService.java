package org.folio.mosaic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.mosaic.client.OrdersClient;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrdersService {

  private final OrdersClient ordersClient;
  private final ConfigurationService configurationService;
  private final MosaicOrderConverter orderConverter;

  /**
   * Creates an order using the new MosaicOrderRequest format.
   * This method handles mapping the request to a CompositePurchaseOrder, applying
   * template values, and creating the order in FOLIO.
   */
  public String createOrder(MosaicOrderRequest orderRequest) {
    var requestOrderTemplateId = orderRequest.getOrderTemplateId();
    var mosaicOrder = orderRequest.getMosaicOrder();
    log.info("createOrder:: Creating mosaic order with title: {}", orderRequest.getMosaicOrder().getTitle());

    String orderTemplateId = requestOrderTemplateId != null
      ? requestOrderTemplateId
      : configurationService.getConfiguration().getDefaultTemplateId();

    var orderTemplate = ordersClient.getOrderTemplateById(orderTemplateId);
    var compositePurchaseOrder = orderConverter.convertToCompositePurchaseOrder(mosaicOrder, orderTemplate);

    var createdOrder = ordersClient.createOrder(compositePurchaseOrder);
    return createdOrder.getPoLines().getFirst().getPoLineNumber();
  }

}
