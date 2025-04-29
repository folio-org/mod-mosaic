package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

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

  @Mock
  private OrdersClient ordersClient;

  @Mock
  private ConfigurationService configurationService;

  @Mock
  private MosaicOrderConverter orderConverter;

  @InjectMocks
  private OrdersService ordersService;

  @Test
  void positive_createOrder_withProvidedTemplateId() {
    var templateId = UUID.randomUUID().toString();
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var request = new MosaicOrderRequest()
      .withOrderTemplateId(templateId)
      .withMosaicOrder(mosaicOrder);

    var orderTemplate = new CompositePurchaseOrder();
    var compositePurchaseOrder = new CompositePurchaseOrder();
    var createdOrder = new CompositePurchaseOrder();
    createdOrder.setPoLines(List.of(new PoLine().withPoLineNumber("POL12345")));

    when(ordersClient.getOrderTemplateById(templateId)).thenReturn(orderTemplate);
    when(orderConverter.convertToCompositePurchaseOrder(mosaicOrder, orderTemplate)).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(request);

    assertEquals("POL12345", result);
    verify(ordersClient).getOrderTemplateById(templateId);
    verify(orderConverter).convertToCompositePurchaseOrder(mosaicOrder, orderTemplate);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

  @Test
  void positive_createOrder_useDefaultTemplate() {
    var defaultTemplateId = UUID.randomUUID().toString();
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var request = new MosaicOrderRequest()
      .withMosaicOrder(mosaicOrder); // No template ID provided

    var orderTemplate = new CompositePurchaseOrder();
    var compositePurchaseOrder = new CompositePurchaseOrder();
    var createdOrder = new CompositePurchaseOrder();
    createdOrder.setPoLines(List.of(new PoLine().withPoLineNumber("POL12345")));

    when(configurationService.getConfiguration()).thenReturn(new MosaicConfiguration().withDefaultTemplateId(defaultTemplateId));
    when(ordersClient.getOrderTemplateById(defaultTemplateId)).thenReturn(orderTemplate);
    when(orderConverter.convertToCompositePurchaseOrder(mosaicOrder, orderTemplate)).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(request);

    assertEquals("POL12345", result);
    verify(configurationService).getConfiguration();
    verify(ordersClient).getOrderTemplateById(defaultTemplateId);
    verify(orderConverter).convertToCompositePurchaseOrder(mosaicOrder, orderTemplate);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

  @Test
  void negative_createOrder_throwClientException() {
    var templateId = UUID.randomUUID().toString();
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var request = new MosaicOrderRequest()
      .withOrderTemplateId(templateId)
      .withMosaicOrder(mosaicOrder);

    var orderTemplate = new CompositePurchaseOrder();
    var compositePurchaseOrder = new CompositePurchaseOrder();

    when(ordersClient.getOrderTemplateById(templateId)).thenReturn(orderTemplate);
    when(orderConverter.convertToCompositePurchaseOrder(mosaicOrder, orderTemplate)).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenThrow(new RuntimeException("Order creation failed"));

    assertThrows(RuntimeException.class, () -> ordersService.createOrder(request));
    verify(ordersClient).getOrderTemplateById(templateId);
    verify(orderConverter).convertToCompositePurchaseOrder(mosaicOrder, orderTemplate);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }
}
