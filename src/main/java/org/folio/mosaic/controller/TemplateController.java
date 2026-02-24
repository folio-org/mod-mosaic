package org.folio.mosaic.controller;

import org.folio.mosaic.rest.resource.TemplateApi;
import org.folio.mosaic.service.TemplateInitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/mosaic")
@RequiredArgsConstructor
public class TemplateController implements TemplateApi {

  private final TemplateInitService templateInitService;

  @Override
  public ResponseEntity<Void> generateDefaultTemplate() {
    templateInitService.createDefaultTemplateIfNeeded();
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

}
