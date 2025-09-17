package org.folio.mosaic.controller;

import org.folio.mosaic.rest.resource.ConfigurationApi;
import org.folio.mosaic.service.ConfigurationService;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/mosaic")
@RequiredArgsConstructor
@Log4j2
public class ConfigurationController implements ConfigurationApi {

  private final ConfigurationService configurationService;

  @Override
  public ResponseEntity<MosaicConfiguration> getConfiguration() {
    return ResponseEntity.ok(configurationService.getConfiguration());
  }

  @Override
  public ResponseEntity<Void> updateConfiguration(MosaicConfiguration configuration) {
    configurationService.updateConfiguration(configuration);
    return ResponseEntity.noContent().build();
  }
}
