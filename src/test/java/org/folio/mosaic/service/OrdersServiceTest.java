package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.client.OrdersClient;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.PoLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@CopilotGenerated
@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

  @Mock private OrdersClient ordersClient;
  @Mock private ConfigurationService configurationService;
  @Mock private MosaicOrderConverter orderConverter;
  @Mock private ObjectMapper objectMapper;
  @InjectMocks private OrdersService ordersService;

  @Test
  void positive_createOrder_withProvidedTemplateId() throws IOException {
    var templateId = UUID.randomUUID().toString();
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var request = new MosaicOrderRequest()
      .withOrderTemplateId(templateId)
      .withMosaicOrder(mosaicOrder);

    var orderTemplate = new CompositePurchaseOrder();
    var poLineTemplate = new PoLine();
    var templatePair = Pair.of(orderTemplate, poLineTemplate);
    var compositePurchaseOrder = new CompositePurchaseOrder();
    var createdOrder = new CompositePurchaseOrder();
    createdOrder.setPoLines(List.of(new PoLine().withPoLineNumber("POL12345")));

    var response = mock(Response.class);
    var body = mock(Response.Body.class);

    when(response.body()).thenReturn(body);
    when(body.asInputStream()).thenReturn(mock(InputStream.class));

    when(ordersClient.getOrderTemplateAsResponse(templateId)).thenReturn(response);
    when(orderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair)).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(request);

    assertEquals("POL12345", result);
    verify(ordersClient).getOrderTemplateAsResponse(templateId);
    verify(orderConverter).convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

  @Test
  void positive_createOrder_useDefaultTemplate() throws IOException {
    var defaultTemplateId = UUID.randomUUID().toString();
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var request = new MosaicOrderRequest()
      .withMosaicOrder(mosaicOrder); // No template ID provided

    var orderTemplate = new CompositePurchaseOrder();
    var poLineTemplate = new PoLine();
    var templatePair = Pair.of(orderTemplate, poLineTemplate);
    var compositePurchaseOrder = new CompositePurchaseOrder();
    var createdOrder = new CompositePurchaseOrder();
    createdOrder.setPoLines(List.of(new PoLine().withPoLineNumber("POL12345")));

    var response = mock(Response.class);
    var body = mock(Response.Body.class);

    when(response.body()).thenReturn(body);
    when(body.asInputStream()).thenReturn(mock(InputStream.class));
    when(configurationService.getConfiguration()).thenReturn(new MosaicConfiguration().withDefaultTemplateId(defaultTemplateId));
    when(ordersClient.getOrderTemplateAsResponse(defaultTemplateId)).thenReturn(response);
    when(orderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair)).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(request);

    assertEquals("POL12345", result);
    verify(configurationService).getConfiguration();
    verify(ordersClient).getOrderTemplateAsResponse(defaultTemplateId);
    verify(orderConverter).convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

  @Test
  void negative_createOrder_throwClientException() throws IOException {
    var templateId = UUID.randomUUID().toString();
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var request = new MosaicOrderRequest()
      .withOrderTemplateId(templateId)
      .withMosaicOrder(mosaicOrder);

    var orderTemplate = new CompositePurchaseOrder();
    var poLineTemplate = new PoLine();
    var templatePair = Pair.of(orderTemplate, poLineTemplate);
    var compositePurchaseOrder = new CompositePurchaseOrder();
    var response = mock(Response.class);
    var body = mock(Response.Body.class);

    when(response.body()).thenReturn(body);
    when(body.asInputStream()).thenReturn(mock(InputStream.class));
    when(ordersClient.getOrderTemplateAsResponse(templateId)).thenReturn(response);
    when(orderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair)).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenThrow(new RuntimeException("Order creation failed"));

    assertThrows(RuntimeException.class, () -> ordersService.createOrder(request));
    verify(ordersClient).getOrderTemplateAsResponse(templateId);
    verify(orderConverter).convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }
}
