package org.folio.mosaic.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.service.OrdersService;
import org.folio.mosaic.service.OrganizationService;
import org.folio.mosaic.service.TemplateInitService;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.PoLine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@CopilotGenerated(partiallyGenerated = true, model = "Claude Opus 4.6")
@WebMvcTest(TemplateController.class)
@Import(TemplateControllerTest.TestConfig.class)
public class TemplateControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoSpyBean
  private TemplateInitService templateInitService;
  @MockitoBean
  private OrdersService ordersService;
  @MockitoBean
  private OrganizationService organizationService;

  @Test
  void testGenerateDefaultTemplate_shouldGenerate() throws Exception {
    when(ordersService.getOrderTemplateById(any())).thenReturn(null);
    doNothing().when(ordersService).createOrderTemplate(any());
    when(organizationService.findByCode(any())).thenReturn(null);
    doNothing().when(organizationService).create(any());

    mockMvc.perform(post("/mosaic/template"))
      .andExpect(status().isCreated());

    verify(templateInitService).createDefaultTemplateIfNeeded();
    verify(ordersService).createOrderTemplate(any());
  }

  @Test
  void testGenerateDefaultTemplate_shouldNotGenerateWhenExists() throws Exception {
    when(ordersService.getOrderTemplateById(any())).thenReturn(Pair.of(new CompositePurchaseOrder(), new PoLine()));

    mockMvc.perform(post("/mosaic/template"))
      .andExpect(status().isCreated());

    verify(templateInitService).createDefaultTemplateIfNeeded();
    verify(ordersService, never()).createOrderTemplate(any());
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public TemplateInitService templateInitService(OrdersService ordersService, OrganizationService organizationService) {
      return new TemplateInitService(organizationService, ordersService, new ObjectMapper());
    }
    @Bean
    public OrdersService ordersService() {
      return Mockito.mock(OrdersService.class);
    }
    @Bean
    public OrganizationService organizationService() {
      return Mockito.mock(OrganizationService.class);
    }
  }

}
