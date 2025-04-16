package org.folio.mosaic.domain.mapper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.mapstruct.Mapping;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "metadata.createdDate", source = "entity.createdDate")
@Mapping(target = "metadata.createdByUserId", source = "entity.createdBy")
@Mapping(target = "metadata.updatedDate", source = "entity.updatedDate")
@Mapping(target = "metadata.updatedByUserId", source = "entity.updatedBy")
public @interface AuditableMapping {

}
