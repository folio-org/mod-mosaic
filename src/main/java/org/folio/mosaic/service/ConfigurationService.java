package org.folio.mosaic.service;

import java.util.UUID;

import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.folio.mosaic.domain.mapper.MosaicConfigurationMapper;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.mosaic.repository.ConfigurationRepository;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@RequiredArgsConstructor
@Log4j2
public class ConfigurationService {

  private final ConfigurationRepository configurationRepository;
  private final MosaicConfigurationMapper mapper;

  public MosaicConfiguration getConfiguration() {
    return mapper.toDto(getConfigurationEntity());
  }

  @Transactional
  public void updateConfiguration(MosaicConfiguration configuration) {
    val existingConfiguration = getConfigurationEntity();
    existingConfiguration.setDefaultTemplateId(UUID.fromString(configuration.getDefaultTemplateId()));
    configurationRepository.save(existingConfiguration);
  }

  private MosaicConfigurationEntity getConfigurationEntity() {
    return configurationRepository.findAll().stream()
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException(MosaicConfigurationEntity.class));
  }
}
