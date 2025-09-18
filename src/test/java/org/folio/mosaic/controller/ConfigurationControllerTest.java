package org.folio.mosaic.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.mosaic.support.CopilotGenerated;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.mosaic.service.ConfigurationService;
import org.folio.mosaic.support.JsonUtils;
import org.folio.mosaic.util.error.ErrorCode;
import org.folio.mosaic.util.error.ErrorUtils;
import org.folio.rest.acq.model.mosaic.MosaicConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

  @Test
  void testGetConfiguration() throws Exception {
    val configuration = new MosaicConfiguration();
    when(configurationService.getConfiguration()).thenReturn(configuration);

    mockMvc.perform(get("/mosaic/configuration"))
      .andExpect(status().isOk())
      .andExpect(content().json(JsonUtils.toJson(configuration)));

    verify(configurationService).getConfiguration();
  }

  @Test
  void testGetConfigurationNotFound() throws Exception {
    val expectedException = new ResourceNotFoundException(MosaicConfiguration.class);
    val expectedError = ErrorUtils.getErrors(expectedException.getMessage(), ErrorCode.NOT_FOUND_ERROR);

    when(configurationService.getConfiguration()).thenThrow(expectedException);

    mockMvc.perform(get("/mosaic/configuration"))
      .andExpect(status().isNotFound())
      .andExpect(content().json(JsonUtils.toJson(expectedError)));

    verify(configurationService).getConfiguration();
  }

  @Test
  void testUpdateConfiguration() throws Exception {
    val configuration = new MosaicConfiguration();

    mockMvc.perform(put("/mosaic/configuration")
        .contentType("application/json")
        .content(JsonUtils.toJson(configuration)))
      .andExpect(status().isNoContent());

    verify(configurationService).updateConfiguration(configuration);
  }

  @Test
  void testUpdateConfigurationNotFound() throws Exception {
    val configuration = new MosaicConfiguration();
    val expectedException = new ResourceNotFoundException(MosaicConfiguration.class);
    val expectedError = ErrorUtils.getErrors(expectedException.getMessage(), ErrorCode.NOT_FOUND_ERROR);

    doThrow(expectedException).when(configurationService).updateConfiguration(configuration);

    mockMvc.perform(put("/mosaic/configuration")
        .contentType("application/json")
        .content(JsonUtils.toJson(configuration)))
      .andExpect(status().isNotFound())
      .andExpect(content().json(JsonUtils.toJson(expectedError)));

    verify(configurationService).updateConfiguration(configuration);
  }
}
