package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.exception.TemplateInitializationException;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.orders.CompositePoLine;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.OrderTemplate;
import org.folio.rest.acq.model.orgs.Organization;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@CopilotGenerated(model = "Claude 3.7 Sonnet Thinking")
class TemplateInitServiceTest {

  @Mock private OrganizationService organizationService;
  @Mock private OrdersService ordersService;
  @Mock private ObjectMapper objectMapper;
  @InjectMocks private TemplateInitService templateInitService;

  @Test
  void testCreateDefaultTemplateIfNeeded_WhenTemplateExists_ShouldSkipCreation() {
    // Given
    var existingTemplate = Pair.of(new CompositePurchaseOrder(), new CompositePoLine());
    when(ordersService.getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID))
        .thenReturn(existingTemplate);

    // When
    templateInitService.createDefaultTemplateIfNeeded();

    // Then
    verify(ordersService).getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID);
    verify(ordersService, never()).createOrderTemplate(any(OrderTemplate.class));
    verify(organizationService, never()).findByCode(any());
  }

  @Test
  void testCreateDefaultTemplateIfNeeded_WhenTemplateDoesNotExist_AndOrganizationExists_ShouldCreateTemplate() throws IOException {
    // Given
    when(ordersService.getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID))
        .thenReturn(null);

    var mockOrderTemplate = new OrderTemplate();
    mockOrderTemplate.setId(TemplateInitService.DEFAULT_TEMPLATE_ID);
    mockOrderTemplate.setTemplateName("Test Template");

    var existingOrganization = new Organization();
    existingOrganization.setId("org-123");
    existingOrganization.setCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE);

    when(objectMapper.readValue(any(InputStream.class), eq(OrderTemplate.class)))
        .thenReturn(mockOrderTemplate);
    when(organizationService.findByCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE))
        .thenReturn(existingOrganization);

    // When
    templateInitService.createDefaultTemplateIfNeeded();

    // Then
    verify(ordersService).getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID);
    verify(organizationService).findByCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE);
    verify(organizationService, never()).create(any(Organization.class));
    verify(ordersService).createOrderTemplate(mockOrderTemplate);
  }

  @Test
  void testCreateDefaultTemplateIfNeeded_WhenTemplateDoesNotExist_AndOrganizationDoesNotExist_ShouldCreateBoth() throws IOException {
    // Given
    when(ordersService.getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID))
        .thenReturn(null);

    var mockOrderTemplate = new OrderTemplate();
    mockOrderTemplate.setId(TemplateInitService.DEFAULT_TEMPLATE_ID);
    mockOrderTemplate.setTemplateName("Test Template");

    var newOrganization = new Organization();
    newOrganization.setId("org-456");
    newOrganization.setCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE);

    when(objectMapper.readValue(any(InputStream.class), eq(OrderTemplate.class)))
        .thenReturn(mockOrderTemplate);
    when(objectMapper.readValue(any(InputStream.class), eq(Organization.class)))
        .thenReturn(newOrganization);
    when(organizationService.findByCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE))
        .thenReturn(null);

    // When
    templateInitService.createDefaultTemplateIfNeeded();

    // Then
    verify(ordersService).getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID);
    verify(organizationService).findByCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE);
    verify(organizationService).create(newOrganization);
    verify(ordersService).createOrderTemplate(mockOrderTemplate);
  }

  @Test
  void testCreateDefaultTemplateIfNeeded_WhenIOExceptionOccurs_ShouldThrowTemplateInitializationException() throws IOException {
    // Given
    when(ordersService.getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID))
        .thenReturn(null);

    when(objectMapper.readValue(any(InputStream.class), eq(OrderTemplate.class)))
        .thenThrow(new IOException("File not found"));

    // When & Then
    assertThrows(TemplateInitializationException.class,
        () -> templateInitService.createDefaultTemplateIfNeeded());

    verify(ordersService).getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID);
    verify(ordersService, never()).createOrderTemplate(any(OrderTemplate.class));
  }

  @Test
  void testCreateDefaultTemplateIfNeeded_WhenOrganizationIOExceptionOccurs_ShouldThrowTemplateInitializationException() throws IOException {
    // Given
    when(ordersService.getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID))
        .thenReturn(null);

    var mockOrderTemplate = new OrderTemplate();

    when(objectMapper.readValue(any(InputStream.class), eq(OrderTemplate.class)))
        .thenReturn(mockOrderTemplate);
    when(organizationService.findByCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE))
        .thenReturn(null);
    when(objectMapper.readValue(any(InputStream.class), eq(Organization.class)))
        .thenThrow(new IOException("Organization file not found"));

    // When & Then
    assertThrows(TemplateInitializationException.class,
        () -> templateInitService.createDefaultTemplateIfNeeded());

    verify(ordersService).getOrderTemplateById(TemplateInitService.DEFAULT_TEMPLATE_ID);
    verify(organizationService).findByCode(TemplateInitService.MOSAIC_ORGANIZATION_CODE);
    verify(ordersService, never()).createOrderTemplate(any(OrderTemplate.class));
  }
}
