package org.folio.mosaic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.exception.TemplateInitializationException;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.OrderTemplate;
import org.folio.rest.acq.model.orders.PoLine;
import org.folio.rest.acq.model.orgs.Organization;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
public class TemplateInitService {
  public static final String DEFAULT_TEMPLATE_ID = "2e1bbcf9-dbef-45d6-b496-cff85a0a6d80";
  public static final String MOSAIC_ORGANIZATION_CODE = "MOSAIC";
  public static final String VENDOR_TEMPLATE_FIELD = "vendor";

  private final OrganizationService organizationService;
  private final OrdersService ordersService;
  private final ObjectMapper objectMapper;

  public void createDefaultTemplateIfNeeded() {
    Pair<CompositePurchaseOrder, PoLine> orderTemplate = ordersService.getOrderTemplateById(DEFAULT_TEMPLATE_ID);
    if (orderTemplate != null) {
      log.info("Default order template with ID {} already exists, skipping creation", DEFAULT_TEMPLATE_ID);
      return;
    }

    try {
      OrderTemplate defaultTemplate = readDefaultOrderTemplate();
      Organization mosaicOrganization = organizationService.findByCode(MOSAIC_ORGANIZATION_CODE);
      if (mosaicOrganization == null) {
        mosaicOrganization = readDefaultOrganization();
        organizationService.create(mosaicOrganization);
      }
      defaultTemplate.getAdditionalProperties().put(VENDOR_TEMPLATE_FIELD, mosaicOrganization.getId());
      ordersService.createOrderTemplate(defaultTemplate);
      log.info("Created default order template with ID: {}", DEFAULT_TEMPLATE_ID);
    } catch (IOException e) {
      log.error("Failed to read default order template from file", e);
      throw new TemplateInitializationException("Failed to create default order template", e);
    }
  }

  private OrderTemplate readDefaultOrderTemplate() throws IOException {
    ClassPathResource resource = new ClassPathResource("default_order_template.json");
    try (var inputStream = resource.getInputStream()) {
      return objectMapper.readValue(inputStream, OrderTemplate.class);
    }
  }

  private Organization readDefaultOrganization() throws IOException {
    ClassPathResource resource = new ClassPathResource("default_mosaic_organization.json");
    try (var inputStream = resource.getInputStream()) {
      return objectMapper.readValue(inputStream, Organization.class);
    }
  }
}
