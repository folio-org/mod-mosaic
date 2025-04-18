package org.folio.mosaic.domain.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.folio.mosaic.domain.entity.base.AuditableEntity;

@Entity
@Table(name = "mosaic_configuration")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class MosaicConfigurationEntity extends AuditableEntity {

  @Id
  private UUID id;

  @Column(name = "default_template_id", nullable = false)
  private UUID defaultTemplateId;

}
