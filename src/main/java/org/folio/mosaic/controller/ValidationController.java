package org.folio.mosaic.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.mosaic.domain.dto.MosaicValidation;
import org.folio.mosaic.rest.resource.ValidateApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.folio.mosaic.domain.dto.MosaicValidation.StatusEnum.SUCCESS;
import static org.springframework.http.HttpStatus.OK;

@Log4j2
@RestController
@RequestMapping("/mosaic")
@RequiredArgsConstructor
public class ValidationController implements ValidateApi {

  @Override
  @GetMapping(value = "/validate")
  public ResponseEntity<MosaicValidation> getValidation() {
    var result = new MosaicValidation();
    result.setStatus(SUCCESS);
    return new ResponseEntity<>(result, OK);
  }
}
