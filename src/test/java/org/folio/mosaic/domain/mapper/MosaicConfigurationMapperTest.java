package org.folio.mosaic.domain.mapper;

import static org.folio.mosaic.support.JsonUtils.getMockData;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MosaicConfigurationMapperTest {

  private static final String MOCK_ENTITY_PATH = "src/test/resources/mock/mapper/mosaic_configuration_entity.json";
  private static final String MOCK_DTO_PATH = "src/test/resources/mock/mapper/mosaic_configuration.json";

  private MosaicConfigurationMapper mapper;
  private MosaicConfigurationEntity entity;
  private MosaicConfiguration dto;

  @BeforeEach
  void setUp() {
    mapper = new MosaicConfigurationMapperImpl();
    entity = getMockData(MOCK_ENTITY_PATH, MosaicConfigurationEntity.class);
    dto = getMockData(MOCK_DTO_PATH, MosaicConfiguration.class);
  }

  @Test
  void testToEntity() {
    MosaicConfigurationEntity convertedEntity = mapper.toEntity(dto);

    assertEquals(entity, convertedEntity);
  }

  @Test
  void testToDto() {
    MosaicConfiguration convertedDto = mapper.toDto(entity);

    assertEquals(dto, convertedDto);
  }

}
