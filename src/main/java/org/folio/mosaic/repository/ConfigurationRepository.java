package org.folio.mosaic.repository;

import java.util.UUID;

import org.folio.mosaic.domain.entity.MosaicConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<MosaicConfigurationEntity, UUID> {

}
