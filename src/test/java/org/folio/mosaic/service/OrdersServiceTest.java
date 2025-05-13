package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.folio.mosaic.client.OrdersClient;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.PoLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import feign.Response;

@ExtendWith(MockitoExtension.class)
@CopilotGenerated(model = "Claude 3.7 Sonnet Thinking")
class OrdersServiceTest {

  @Mock private OrdersClient ordersClient;
  @Mock private ConfigurationService configurationService;
  @Mock private MosaicOrderConverter orderConverter;
  @Mock private ObjectMapper objectMapper;
  @InjectMocks private OrdersService ordersService;

  @Test
  @SuppressWarnings("unchecked")
  void testCreateOrderProvidedTemplateId() throws Exception {
    var templateId = "templateId";
    var mosaicOrder = new MosaicOrder();
    mosaicOrder.setTitle("Test Order");
    var orderRequest = new MosaicOrderRequest();
    orderRequest.setOrderTemplateId(templateId);
    orderRequest.setMosaicOrder(mosaicOrder);

    var response = mock(Response.class);
    var body = mock(Response.Body.class);
    when(ordersClient.getOrderTemplateAsResponse(templateId)).thenReturn(response);
    when(response.body()).thenReturn(body);
    var dummyBytes = "dummy".getBytes();
    var inputStream = new ByteArrayInputStream(dummyBytes);
    when(body.asInputStream()).thenReturn(inputStream);

    var compositeTemplate = new CompositePurchaseOrder();
    compositeTemplate.setId("orderId");
    var poLineTemplate = new PoLine();
    poLineTemplate.setId("poLineId");

    when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
      .thenAnswer(invocation -> {
        var typeRef = invocation.getArgument(1, TypeReference.class);
        if (typeRef.getType().getTypeName().contains("CompositePurchaseOrder")) {
          return compositeTemplate;
        } else {
          return poLineTemplate;
        }
      });

    var expectedCompositeOrder = new CompositePurchaseOrder();
    var poLine = new PoLine();
    poLine.setPoLineNumber("POL12345");
    expectedCompositeOrder.setPoLines(List.of(poLine));
    when(orderConverter.convertToCompositePurchaseOrder(eq(mosaicOrder), any())).thenReturn(expectedCompositeOrder);
    when(ordersClient.createOrder(expectedCompositeOrder)).thenReturn(expectedCompositeOrder);

    var result = ordersService.createOrder(orderRequest);
    assertEquals("POL12345", result);

    verify(ordersClient).getOrderTemplateAsResponse(templateId);
    verify(orderConverter).convertToCompositePurchaseOrder(eq(mosaicOrder), any());
    verify(ordersClient).createOrder(expectedCompositeOrder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testCreateOrderDefaultTemplate() throws Exception {
    var defaultTemplateId = "defaultTemplate";
    var mosaicOrder = new MosaicOrder();
    mosaicOrder.setTitle("Default Order");
    var orderRequest = new MosaicOrderRequest();
    orderRequest.setMosaicOrder(mosaicOrder);

    when(configurationService.getConfiguration()).thenReturn(new MosaicConfiguration().withDefaultTemplateId(defaultTemplateId));

    var response = mock(Response.class);
    var body = mock(Response.Body.class);
    when(ordersClient.getOrderTemplateAsResponse(defaultTemplateId)).thenReturn(response);
    when(response.body()).thenReturn(body);
    var dummyBytes = "dummy".getBytes();
    var inputStream = new ByteArrayInputStream(dummyBytes);
    when(body.asInputStream()).thenReturn(inputStream);

    var compositeTemplate = new CompositePurchaseOrder();
    compositeTemplate.setId("orderId");
    var poLineTemplate = new PoLine();
    poLineTemplate.setId("poLineId");

    when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
      .thenAnswer(invocation -> {
        var typeRef = invocation.getArgument(1, TypeReference.class);
        if (typeRef.getType().getTypeName().contains("CompositePurchaseOrder")) {
          return compositeTemplate;
        } else {
          return poLineTemplate;
        }
      });

    var expectedCompositeOrder = new CompositePurchaseOrder();
    var poLine = new PoLine();
    poLine.setPoLineNumber("POL12345");
    expectedCompositeOrder.setPoLines(List.of(poLine));
    when(orderConverter.convertToCompositePurchaseOrder(eq(mosaicOrder), any())).thenReturn(expectedCompositeOrder);
    when(ordersClient.createOrder(expectedCompositeOrder)).thenReturn(expectedCompositeOrder);

    var result = ordersService.createOrder(orderRequest);
    assertEquals("POL12345", result);

    verify(configurationService).getConfiguration();
    verify(ordersClient).getOrderTemplateAsResponse(defaultTemplateId);
    verify(orderConverter).convertToCompositePurchaseOrder(eq(mosaicOrder), any());
    verify(ordersClient).createOrder(expectedCompositeOrder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testCreateOrderThrowsResourceNotFoundException() throws Exception {
    var templateId = "templateId";
    var mosaicOrder = new MosaicOrder();
    mosaicOrder.setTitle("Test Order");
    var orderRequest = new MosaicOrderRequest();
    orderRequest.setOrderTemplateId(templateId);
    orderRequest.setMosaicOrder(mosaicOrder);

    var response = mock(Response.class);
    var body = mock(Response.Body.class);
    when(ordersClient.getOrderTemplateAsResponse(templateId)).thenReturn(response);
    when(response.body()).thenReturn(body);
    var dummyBytes = "dummy".getBytes();
    var inputStream = new ByteArrayInputStream(dummyBytes);
    when(body.asInputStream()).thenReturn(inputStream);

    // Return an order without an id to trigger ResourceNotFoundException.
    when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
      .thenAnswer(invocation -> {
        var typeRef = invocation.getArgument(1, TypeReference.class);
        if (typeRef.getType().getTypeName().contains("CompositePurchaseOrder")) {
          return new CompositePurchaseOrder();
        } else {
          return new PoLine();
        }
      });

    assertThrows(ResourceNotFoundException.class, () -> ordersService.createOrder(orderRequest));

    verify(ordersClient).getOrderTemplateAsResponse(templateId);
    verify(objectMapper, atLeast(2)).readValue(any(byte[].class), any(TypeReference.class));
  }
}
