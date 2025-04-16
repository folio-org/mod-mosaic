package org.folio.mosaic.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.mosaic.CopilotGenerated;
import org.folio.mosaic.service.ConfigurationService;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;


@CopilotGenerated(partiallyGenerated = true)
@WebMvcTest(ConfigurationController.class)
class ConfigurationControllerTest {

  @MockitoBean
  private ConfigurationService configurationService;
  @Autowired
  private ConfigurationController configurationController;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void getConfigurationReturnsOkWithConfiguration() throws Exception {
    val configuration = new MosaicConfiguration();
    when(configurationService.getConfiguration()).thenReturn(configuration);

    mockMvc.perform(get("/mosaic/configuration"))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(configuration)));

    verify(configurationService).getConfiguration();
  }

  @Test
  void saveConfigurationReturnsCreatedWithSavedConfiguration() throws Exception {
    val configuration = new MosaicConfiguration();
    val savedConfiguration = new MosaicConfiguration();
    when(configurationService.saveConfiguration(configuration)).thenReturn(savedConfiguration);

    mockMvc.perform(post("/mosaic/configuration")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(configuration)))
        .andExpect(status().isCreated())
        .andExpect(content().json(objectMapper.writeValueAsString(savedConfiguration)));

    verify(configurationService).saveConfiguration(configuration);
  }

  @Test
  void updateConfigurationReturnsNoContent() throws Exception {
    val configuration = new MosaicConfiguration();

    mockMvc.perform(put("/mosaic/configuration")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(configuration)))
        .andExpect(status().isNoContent());

    verify(configurationService).updateConfiguration(configuration);
  }

  @Test
  void deleteConfigurationReturnsNoContent() throws Exception {
    mockMvc.perform(delete("/mosaic/configuration"))
        .andExpect(status().isNoContent());

    verify(configurationService).deleteConfiguration();
  }

}
