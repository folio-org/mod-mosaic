package org.folio.mosaic.service;

import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FolioTenantService extends TenantService {

  private final TemplateInitService templateInitService;

  public FolioTenantService(JdbcTemplate jdbcTemplate,
    FolioExecutionContext context,
    FolioSpringLiquibase folioSpringLiquibase,
    TemplateInitService templateInitService) {
    super(jdbcTemplate, context, folioSpringLiquibase);
    this.templateInitService = templateInitService;
  }

  @Override
  protected void afterTenantUpdate(TenantAttributes tenantAttributes) {
    templateInitService.createDefaultTemplateIfNeeded();
  }
}
