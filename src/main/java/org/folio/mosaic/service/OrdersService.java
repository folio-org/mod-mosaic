package org.folio.mosaic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.client.OrdersClient;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.OrderTemplate;
import org.folio.rest.acq.model.orders.PoLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    var requestTemplateId = orderRequest.getOrderTemplateId();
    var mosaicOrder = orderRequest.getMosaicOrder();

    log.info("createOrder:: Creating mosaic order with title: {} and requestTemplateId: {}", mosaicOrder.getTitle(),
      requestTemplateId);
    var templatePair = getOrderTemplatePair(mosaicOrder.getTitle(), requestTemplateId);
    var compositePurchaseOrder = orderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var createdOrder = ordersClient.createOrder(compositePurchaseOrder);

    return createdOrder.getPoLines().getFirst().getPoLineNumber();
  }

  @SneakyThrows
  private Pair<CompositePurchaseOrder, PoLine> getOrderTemplatePair(String title, String requestTemplateId) {
    var templateId = requestTemplateId != null
      ? requestTemplateId : configurationService.getConfiguration().getDefaultTemplateId();

    try (var response = ordersClient.getOrderTemplateAsResponse(templateId)) {
      return responseToOrderAndPoLineObjects(title, response, templateId);
    }
  }

  private Pair<CompositePurchaseOrder, PoLine> responseToOrderAndPoLineObjects(String title, Response response,
                                                                               String templateId) throws IOException {
    try (var inputStream = response.body().asInputStream()) {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      inputStream.transferTo(byteArrayOutputStream);

      var byteArray = byteArrayOutputStream.toByteArray();
      var order = objectMapper.readValue(byteArray, new TypeReference<CompositePurchaseOrder>() {});
      var poLine = objectMapper.readValue(byteArray, new TypeReference<PoLine>() {});
      if (order == null || order.getId() == null) {
        log.warn("getOrderTemplatePair:: No template found for mosaicOrder: {} with id: {}", title, templateId);
        throw new ResourceNotFoundException(OrderTemplate.class);
      }

      return Pair.of(order, poLine);
    }
  }
}
