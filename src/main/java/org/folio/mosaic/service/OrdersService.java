package org.folio.mosaic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
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
    var templatePair = getOrderTemplatePair(requestTemplateId);
    var compositePurchaseOrder = orderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var createdOrder = ordersClient.createOrder(compositePurchaseOrder);

    return createdOrder.getPoLines().getFirst().getPoLineNumber();
  }

  @SneakyThrows
  private Pair<CompositePurchaseOrder, PoLine> getOrderTemplatePair(String requestTemplateId) {
    var templateId = StringUtils.isNotBlank(requestTemplateId)
      ? requestTemplateId : configurationService.getConfiguration().getDefaultTemplateId();

    Pair<CompositePurchaseOrder, PoLine> pair = getOrderTemplateById(templateId);
    if (pair == null) {
      log.warn("getOrderTemplatePair:: No template or default template was found for mosaicOrder with templateId: {}", templateId);
      throw new ResourceNotFoundException(OrderTemplate.class);
    }
    return pair;
  }

  @SneakyThrows
  public Pair<CompositePurchaseOrder, PoLine> getOrderTemplateById(String templateId) {
    try (var response = ordersClient.getOrderTemplateAsResponse(templateId)) {
      return responseToOrderAndPoLineObjects(response);
    }
  }

  public void createOrderTemplate(OrderTemplate orderTemplate) {
    ordersClient.createOrderTemplate(orderTemplate);
  }

  private Pair<CompositePurchaseOrder, PoLine> responseToOrderAndPoLineObjects(Response response) throws IOException {
    try (var inputStream = response.body().asInputStream()) {
      var byteArrayOutputStream = new ByteArrayOutputStream();
      inputStream.transferTo(byteArrayOutputStream);

      var byteArray = byteArrayOutputStream.toByteArray();
      var order = objectMapper.readValue(byteArray, new TypeReference<CompositePurchaseOrder>() {});
      var poLine = objectMapper.readValue(byteArray, new TypeReference<PoLine>() {});
      if (order == null || order.getId() == null) {
        return null;
      }

      return Pair.of(order, poLine);
    }
  }
}
