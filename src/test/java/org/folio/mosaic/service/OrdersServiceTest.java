package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.folio.mosaic.support.CopilotGenerated;
import org.folio.mosaic.client.OrdersClient;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.folio.rest.acq.model.orders.CompositePoLine;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
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

  @InjectMocks
  private OrdersService ordersService;

  @Test
  void positive_createOrder() {
    CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();
    compositePurchaseOrder.setPoNumber("PO12345");
    var templateId = UUID.randomUUID();

    CompositePurchaseOrder createdOrder = new CompositePurchaseOrder();
    createdOrder.setCompositePoLines(List.of(new CompositePoLine().withPoLineNumber("POL12345")));

    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(templateId, compositePurchaseOrder);

    assertEquals("POL12345", result);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

  @Test
  void positive_createOrder_useTemplateId() {
    CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();
    compositePurchaseOrder.setPoNumber("PO12345");
    var templateId = UUID.randomUUID();

    CompositePurchaseOrder createdOrder = new CompositePurchaseOrder();
    createdOrder.setCompositePoLines(List.of(new CompositePoLine().withPoLineNumber("POL12345")));

    when(ordersClient.getOrderTemplateById(any())).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(templateId, compositePurchaseOrder);

    assertEquals("POL12345", result);
    verify(ordersClient).getOrderTemplateById(any());
  }

  @Test
  void positive_createOrder_useDefaultTemplate() {
    CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();
    compositePurchaseOrder.setPoNumber("PO12345");
    var defaultTemplateId = UUID.randomUUID();

    CompositePurchaseOrder createdOrder = new CompositePurchaseOrder();
    createdOrder.setCompositePoLines(List.of(new CompositePoLine().withPoLineNumber("POL12345")));

    when(configurationService.getConfiguration()).thenReturn(new MosaicConfiguration().withDefaultTemplateId(defaultTemplateId.toString()));
    when(ordersClient.getOrderTemplateById(any())).thenReturn(compositePurchaseOrder);
    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(null, compositePurchaseOrder);

    assertEquals("POL12345", result);
    verify(ordersClient).getOrderTemplateById(any());
  }

  @Test
  void negative_createOrder_throwClientException() {
    CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();
    compositePurchaseOrder.setPoNumber("PO12345");
    var templateId = UUID.randomUUID();

    when(ordersClient.createOrder(compositePurchaseOrder)).thenThrow(new RuntimeException("Order creation failed"));

    assertThrows(RuntimeException.class, () -> ordersService.createOrder(templateId, compositePurchaseOrder));
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

}
