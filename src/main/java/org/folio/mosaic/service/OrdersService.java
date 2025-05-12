package org.folio.mosaic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.client.OrdersClient;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.PoLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Log4j2
@Service
@RequiredArgsConstructor
public class OrdersService {

  private final OrdersClient ordersClient;
  private final ConfigurationService configurationService;
  private final MosaicOrderConverter orderConverter;
  private final ObjectMapper objectMapper;

  /**
   * Creates an order using the new MosaicOrderRequest format.
   * This method handles mapping the request to a CompositePurchaseOrder, applying
   * template values, and creating the order in FOLIO.
   */
  public String createOrder(MosaicOrderRequest orderRequest) {
    var requestOrderTemplateId = orderRequest.getOrderTemplateId();
    var mosaicOrder = orderRequest.getMosaicOrder();

    log.info("createOrder:: Creating mosaic order with title: {} and orderTemplateId: {}", mosaicOrder.getTitle(),
      requestOrderTemplateId);
    var templatePair = getOrderTemplatePair(requestOrderTemplateId);
    var compositePurchaseOrder = orderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var createdOrder = ordersClient.createOrder(compositePurchaseOrder);

    return createdOrder.getPoLines().getFirst().getPoLineNumber();
  }

  @SneakyThrows
  private Pair<CompositePurchaseOrder, PoLine> getOrderTemplatePair(String requestOrderTemplateId) {
    var orderTemplateId = requestOrderTemplateId != null
      ? requestOrderTemplateId : configurationService.getConfiguration().getDefaultTemplateId();

    var response = ordersClient.getOrderTemplateAsResponse(orderTemplateId);
    try (var inputStream = response.body().asInputStream()) {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      inputStream.transferTo(byteArrayOutputStream);

      var byteArray = byteArrayOutputStream.toByteArray();
      var order = objectMapper.readValue(byteArray, new TypeReference<CompositePurchaseOrder>() {});
      var poLine = objectMapper.readValue(byteArray, new TypeReference<PoLine>() {});
      return Pair.of(order, poLine);
    }
  }

}
