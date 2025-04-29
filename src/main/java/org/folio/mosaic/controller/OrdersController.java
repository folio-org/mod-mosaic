package org.folio.mosaic.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.mosaic.rest.resource.OrdersApi;
import org.folio.mosaic.service.OrdersService;
import org.folio.rest.acq.model.mosaic.MosaicOrderRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mosaic")
@RequiredArgsConstructor
@Log4j2
public class OrdersController implements OrdersApi {
  private final OrdersService ordersService;

  @Override
  public ResponseEntity<String> createMosaicOrder(MosaicOrderRequest mosaicOrderRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ordersService.createOrder(mosaicOrderRequest));
  }
}
