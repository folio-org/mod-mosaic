package org.folio.mosaic.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.folio.mosaic.service.OrdersService;
import org.folio.mosaic.support.JsonUtils;
import org.folio.mosaic.util.error.ErrorUtils;
import org.folio.mosaic.util.error.ErrorCode;
import org.folio.rest.acq.model.orders.CompositePoLine;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import feign.FeignException;
import feign.Request;
import lombok.val;

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

  @MockitoBean
  private OrdersService ordersService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testCreateOrder() throws Exception {
    val poLineNumber = "12345";
    val mosaicOrder = new CompositePurchaseOrder().withCompositePoLines(List.of(new CompositePoLine().withPoLineNumber(poLineNumber)));
    var templateId = UUID.randomUUID();

    when(ordersService.createOrder(templateId, mosaicOrder)).thenReturn(poLineNumber);

    mockMvc.perform(post("/mosaic/orders?templateId=" + templateId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtils.toJson(mosaicOrder)))
      .andExpect(status().isCreated())
      .andExpect(content().string(poLineNumber));

    verify(ordersService).createOrder(templateId, mosaicOrder);
  }

  @ParameterizedTest
  @MethodSource("createOrderExceptionProvider")
  void testCreateOrderThrowsException(int statusCode, ErrorCode errorCode, Throwable throwable) throws Exception {
    val mosaicOrder = new CompositePurchaseOrder();
    val expectedError = ErrorUtils.getErrors(errorCode.toError());
    var templateId = UUID.randomUUID();

    when(ordersService.createOrder(templateId, mosaicOrder)).thenThrow(throwable);

    mockMvc.perform(post("/mosaic/orders?templateId=" + templateId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtils.toJson(mosaicOrder)))
      .andExpect(status().is(statusCode))
      .andExpect(content().json(JsonUtils.toJson(expectedError)));

    verify(ordersService).createOrder(templateId, mosaicOrder);
  }

  private static Stream<Arguments> createOrderExceptionProvider() {
    val req = mock(Request.class);
    return Stream.of(
      Arguments.of(HttpStatus.NOT_FOUND.value(), ErrorCode.NOT_FOUND_ERROR,
        new FeignException.NotFound(ErrorCode.NOT_FOUND_ERROR.getDescription(), req, null, null)),
      Arguments.of(HttpStatus.UNPROCESSABLE_ENTITY.value(), ErrorCode.VALIDATION_ERROR,
        new FeignException.UnprocessableEntity(ErrorCode.VALIDATION_ERROR.getDescription(), req, null, null)),
      Arguments.of(HttpStatus.BAD_REQUEST.value(), ErrorCode.BAD_REQUEST_ERROR,
        new FeignException.FeignClientException(HttpStatus.BAD_REQUEST.value(), ErrorCode.BAD_REQUEST_ERROR.getDescription(), req, null, null)),
      Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_ERROR,
        new FeignException.FeignServerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_ERROR.getDescription(), req, null, null)),
      Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.UNKNOWN_ERROR,
        new RuntimeException(ErrorCode.UNKNOWN_ERROR.getDescription()))
    );
  }

}
