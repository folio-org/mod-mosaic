package org.folio.mosaic.service;

import java.util.UUID;

import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.folio.mosaic.exception.ResourceAlreadyExistException;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.mosaic.domain.mapper.MosaicConfigurationMapper;
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
  public MosaicConfiguration saveConfiguration(MosaicConfiguration configuration) {
    validateConfigurationNotExists();
    if (configuration.getId() == null) {
      configuration.setId(UUID.randomUUID().toString());
    }
    val entity = configurationRepository.save(mapper.toEntity(configuration));
    return mapper.toDto(entity);
  }

  @Transactional
  public void updateConfiguration(MosaicConfiguration configuration) {
    val existingConfiguration = getConfigurationEntity();
    existingConfiguration.setDefaultTemplateId(UUID.fromString(configuration.getDefaultTemplateId()));
    configurationRepository.save(existingConfiguration);
  }

  public void deleteConfiguration() {
    configurationRepository.deleteAll();
  }

  private MosaicConfigurationEntity getConfigurationEntity() {
    return configurationRepository.findAll().stream()
      .findFirst()
      .orElseThrow(() -> new ResourceNotFoundException(MosaicConfigurationEntity.class));
  }

  private void validateConfigurationNotExists() {
    if (configurationRepository.count() != 0) {
      throw new ResourceAlreadyExistException(MosaicConfigurationEntity.class);
    }
  }

}
