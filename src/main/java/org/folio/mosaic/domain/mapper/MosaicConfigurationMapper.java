package org.folio.mosaic.domain.mapper;

import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MosaicConfigurationMapper {

  @AuditableEntityMapping
  MosaicConfigurationEntity toEntity(MosaicConfiguration dto);

  @AuditableMapping
  @Mapping(target = "withId", ignore = true)
  @Mapping(target = "withDefaultTemplateId", ignore = true)
  @Mapping(target = "withMetadata", ignore = true)
  MosaicConfiguration toDto(MosaicConfigurationEntity entity);

}
