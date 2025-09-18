package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.folio.mosaic.support.CopilotGenerated;
import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.folio.mosaic.domain.mapper.MosaicConfigurationMapper;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.mosaic.repository.ConfigurationRepository;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@CopilotGenerated
@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

  @Mock
  private ConfigurationRepository configurationRepository;
  @Mock
  private MosaicConfigurationMapper mapper;
  @InjectMocks
  private ConfigurationService configurationService;

  @Test
  void testGetConfiguration() {
    MosaicConfigurationEntity entity = new MosaicConfigurationEntity();
    MosaicConfiguration dto = new MosaicConfiguration();

    when(configurationRepository.findAll()).thenReturn(List.of(entity));
    when(mapper.toDto(entity)).thenReturn(dto);

    MosaicConfiguration result = configurationService.getConfiguration();

    assertEquals(dto, result);
    verify(configurationRepository).findAll();
    verify(mapper).toDto(entity);
  }

  @Test
  void testGetConfigurationNotFound() {
    when(configurationRepository.findAll()).thenReturn(Collections.emptyList());

    assertThrows(ResourceNotFoundException.class, () -> configurationService.getConfiguration());
    verify(configurationRepository).findAll();
  }

  @Test
  void testUpdateConfiguration() {
    MosaicConfiguration configuration = new MosaicConfiguration();
    configuration.setDefaultTemplateId(UUID.randomUUID().toString());
    MosaicConfigurationEntity existingEntity = new MosaicConfigurationEntity();

    when(configurationRepository.findAll()).thenReturn(List.of(existingEntity));

    configurationService.updateConfiguration(configuration);

    assertEquals(UUID.fromString(configuration.getDefaultTemplateId()), existingEntity.getDefaultTemplateId());
    verify(configurationRepository).findAll();
    verify(configurationRepository).save(existingEntity);
  }

  @Test
  void testUpdateConfigurationNotFound() {
    when(configurationRepository.findAll()).thenReturn(Collections.emptyList());

    MosaicConfiguration configuration = new MosaicConfiguration();

    assertThrows(ResourceNotFoundException.class, () -> configurationService.updateConfiguration(configuration));
    verify(configurationRepository).findAll();
  }
}
