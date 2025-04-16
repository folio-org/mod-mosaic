package org.folio.mosaic.domain.mapper;

import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MosaicConfigurationMapper {

  MosaicConfigurationEntity toEntity(MosaicConfiguration dto);

  MosaicConfiguration toDto(MosaicConfigurationEntity entity);

}
