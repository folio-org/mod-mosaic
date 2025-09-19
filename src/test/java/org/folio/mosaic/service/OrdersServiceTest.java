package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.folio.rest.acq.model.orders.CompositePoLine;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.OrderTemplate;
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
    // Given
    var templateId = "templateId";
    var orderRequest = createOrderRequest(templateId, "Test Order");

    var compositeTemplate = new CompositePurchaseOrder();
    compositeTemplate.setId("orderId");
    var poLineTemplate = new CompositePoLine();
    poLineTemplate.setId("poLineId");

    setupMockResponse(templateId, compositeTemplate, poLineTemplate);

    var expectedCompositeOrder = new CompositePurchaseOrder();
    var poLine = new CompositePoLine();
    poLine.setPoLineNumber("POL12345");
    expectedCompositeOrder.setCompositePoLines(List.of(poLine));
    when(orderConverter.convertToCompositePurchaseOrder(eq(orderRequest.getMosaicOrder()), any())).thenReturn(expectedCompositeOrder);
    when(ordersClient.createOrder(expectedCompositeOrder)).thenReturn(expectedCompositeOrder);

    // When
    var result = ordersService.createOrder(orderRequest);

    // Then
    assertEquals("POL12345", result);
    verify(ordersClient).getOrderTemplateAsResponse(templateId);
    verify(orderConverter).convertToCompositePurchaseOrder(eq(orderRequest.getMosaicOrder()), any());
    verify(ordersClient).createOrder(expectedCompositeOrder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testCreateOrderDefaultTemplate() throws Exception {
    // Given
    var defaultTemplateId = "defaultTemplate";
    var orderRequest = createOrderRequest(null, "Default Order"); // null templateId to use default

    when(configurationService.getConfiguration()).thenReturn(new MosaicConfiguration().withDefaultTemplateId(defaultTemplateId));

    var compositeTemplate = new CompositePurchaseOrder();
    compositeTemplate.setId("orderId");
    var poLineTemplate = new CompositePoLine();
    poLineTemplate.setId("poLineId");

    setupMockResponse(defaultTemplateId, compositeTemplate, poLineTemplate);

    var expectedCompositeOrder = new CompositePurchaseOrder();
    var poLine = new CompositePoLine();
    poLine.setPoLineNumber("POL12345");
    expectedCompositeOrder.setCompositePoLines(List.of(poLine));
    when(orderConverter.convertToCompositePurchaseOrder(eq(orderRequest.getMosaicOrder()), any())).thenReturn(expectedCompositeOrder);
    when(ordersClient.createOrder(expectedCompositeOrder)).thenReturn(expectedCompositeOrder);

    // When
    var result = ordersService.createOrder(orderRequest);

    // Then
    assertEquals("POL12345", result);
    verify(configurationService).getConfiguration();
    verify(ordersClient).getOrderTemplateAsResponse(defaultTemplateId);
    verify(orderConverter).convertToCompositePurchaseOrder(eq(orderRequest.getMosaicOrder()), any());
    verify(ordersClient).createOrder(expectedCompositeOrder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testCreateOrderThrowsResourceNotFoundException() throws Exception {
    // Given
    var templateId = "templateId";
    var orderRequest = createOrderRequest(templateId, "Test Order");

    // Setup mock to return order without ID (triggers ResourceNotFoundException)
    var compositeTemplateWithoutId = new CompositePurchaseOrder(); // id is null
    var poLineTemplate = new CompositePoLine();
    setupMockResponse(templateId, compositeTemplateWithoutId, poLineTemplate);

    // When & Then
    assertThrows(ResourceNotFoundException.class, () -> ordersService.createOrder(orderRequest));

    verify(ordersClient).getOrderTemplateAsResponse(templateId);
    verify(objectMapper, atLeast(2)).readValue(any(byte[].class), any(TypeReference.class));
  }

  @Test
  void testCreateOrderTemplate() {
    // Given
    var orderTemplate = new OrderTemplate();
    orderTemplate.setId("template-123");
    orderTemplate.setTemplateName("Test Template");
    orderTemplate.setTemplateDescription("A test template for orders");

    // When
    ordersService.createOrderTemplate(orderTemplate);

    // Then
    verify(ordersClient).createOrderTemplate(orderTemplate);
  }

  @Test
  void testGetOrderTemplateById_WhenTemplateExists_ShouldReturnTemplate() throws Exception {
    // Given
    var templateId = "template-123";
    var compositeTemplate = new CompositePurchaseOrder();
    compositeTemplate.setId("order-123");
    var poLineTemplate = new CompositePoLine();
    poLineTemplate.setId("poline-123");

    setupMockResponse(templateId, compositeTemplate, poLineTemplate);

    // When
    var result = ordersService.getOrderTemplateById(templateId);

    // Then
    assertEquals(compositeTemplate, result.getLeft());
    assertEquals(poLineTemplate, result.getRight());
    verify(ordersClient).getOrderTemplateAsResponse(templateId);
  }

  @Test
  void testGetOrderTemplateById_WhenTemplateDoesNotExist_ShouldReturnNull() throws Exception {
    // Given
    var templateId = "non-existent-template";
    var compositeTemplateWithoutId = new CompositePurchaseOrder(); // id is null by default
    var poLineTemplate = new CompositePoLine();

    setupMockResponse(templateId, compositeTemplateWithoutId, poLineTemplate);

    // When
    var result = ordersService.getOrderTemplateById(templateId);

    // Then
    assertNull(result);
    verify(ordersClient).getOrderTemplateAsResponse(templateId);
  }

  // Helper method to create a standard order request
  private MosaicOrderRequest createOrderRequest(String templateId, String title) {
    var mosaicOrder = new MosaicOrder();
    mosaicOrder.setTitle(title);
    var orderRequest = new MosaicOrderRequest();
    orderRequest.setOrderTemplateId(templateId);
    orderRequest.setMosaicOrder(mosaicOrder);
    return orderRequest;
  }

  // Helper method to setup mock response with template objects
  private void setupMockResponse(String templateId, CompositePurchaseOrder order, CompositePoLine poLine) throws Exception {
    var response = mock(Response.class);
    var body = mock(Response.Body.class);
    when(ordersClient.getOrderTemplateAsResponse(templateId)).thenReturn(response);
    when(response.body()).thenReturn(body);
    when(body.asInputStream()).thenReturn(new ByteArrayInputStream("dummy".getBytes()));

    when(objectMapper.readValue(any(byte[].class), any(TypeReference.class)))
      .thenAnswer(invocation -> {
        var typeRef = invocation.getArgument(1, TypeReference.class);
        if (typeRef.getType().getTypeName().contains("CompositePurchaseOrder")) {
          return order;
        } else {
          return poLine;
        }
      });
  }
}
