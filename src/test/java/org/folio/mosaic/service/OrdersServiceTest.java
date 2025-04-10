package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.folio.mosaic.CopilotGenerated;
import org.folio.mosaic.client.OrdersClient;
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

  @InjectMocks
  private OrdersService ordersService;

  @Test
  void testCreateOrder() {
    CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();
    compositePurchaseOrder.setPoNumber("PO12345");

    CompositePurchaseOrder createdOrder = new CompositePurchaseOrder();
    createdOrder.setCompositePoLines(List.of(new CompositePoLine().withPoLineNumber("POL12345")));

    when(ordersClient.createOrder(compositePurchaseOrder)).thenReturn(createdOrder);

    String result = ordersService.createOrder(compositePurchaseOrder);

    assertEquals("POL12345", result);
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

  @Test
  void testCreateOrderClientException() {
    CompositePurchaseOrder compositePurchaseOrder = new CompositePurchaseOrder();
    compositePurchaseOrder.setPoNumber("PO12345");

    when(ordersClient.createOrder(compositePurchaseOrder)).thenThrow(new RuntimeException("Order creation failed"));

    assertThrows(RuntimeException.class, () -> ordersService.createOrder(compositePurchaseOrder));
    verify(ordersClient).createOrder(compositePurchaseOrder);
  }

}
