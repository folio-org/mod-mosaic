package org.folio.mosaic.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import lombok.val;
import org.folio.mosaic.service.OrdersService;
import org.folio.mosaic.support.JsonUtils;
import org.folio.mosaic.util.error.ErrorCode;
import org.folio.mosaic.util.error.ErrorUtils;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

  @MockitoBean
  private OrdersService ordersService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testCreateOrder() throws Exception {
    var poLineNumber = "12345";
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var orderRequest = new MosaicOrderRequest().withMosaicOrder(mosaicOrder);

    when(ordersService.createOrder(orderRequest)).thenReturn(poLineNumber);

    mockMvc.perform(post("/mosaic/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtils.toJson(orderRequest)))
      .andExpect(status().isCreated())
      .andExpect(content().string(poLineNumber));

    verify(ordersService).createOrder(orderRequest);
  }

  @ParameterizedTest
  @MethodSource("createOrderExceptionProvider")
  void testCreateOrderThrowsException(int statusCode, ErrorCode errorCode, Throwable throwable) throws Exception {
    var mosaicOrder = new MosaicOrder().withTitle("Test Book");
    var orderRequest = new MosaicOrderRequest().withMosaicOrder(mosaicOrder);
    var expectedError = ErrorUtils.getErrors(errorCode.toError());

    when(ordersService.createOrder(orderRequest)).thenThrow(throwable);

    mockMvc.perform(post("/mosaic/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtils.toJson(orderRequest)))
      .andExpect(status().is(statusCode))
      .andExpect(content().json(JsonUtils.toJson(expectedError)));

    verify(ordersService).createOrder(orderRequest);
  }

  private static Stream<Arguments> createOrderExceptionProvider() {
    val headers = new HttpHeaders();
    return Stream.of(
      Arguments.of(HttpStatus.NOT_FOUND.value(), ErrorCode.NOT_FOUND_ERROR, HttpClientErrorException.create(
        ErrorCode.NOT_FOUND_ERROR.getDescription(), HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), headers, null, null)),
      Arguments.of(HttpStatus.UNPROCESSABLE_CONTENT.value(), ErrorCode.VALIDATION_ERROR, HttpClientErrorException.create(
        ErrorCode.VALIDATION_ERROR.getDescription(), HttpStatus.UNPROCESSABLE_CONTENT, HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(), headers, null, null)),
      Arguments.of(HttpStatus.BAD_REQUEST.value(), ErrorCode.BAD_REQUEST_ERROR, HttpClientErrorException.create(
        ErrorCode.BAD_REQUEST_ERROR.getDescription(), HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), headers, null, null)),
      Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.INTERNAL_ERROR, HttpServerErrorException.create(
        ErrorCode.INTERNAL_ERROR.getDescription(), HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), headers, null, null)),
      Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.UNKNOWN_ERROR,
        new RuntimeException(ErrorCode.UNKNOWN_ERROR.getDescription()))
    );
  }

}
