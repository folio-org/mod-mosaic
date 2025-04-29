
package org.folio.mosaic.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.mosaic.support.CopilotGenerated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@CopilotGenerated(model = "o3-mini")
@WebMvcTest(ValidationController.class)
class ValidationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testGetValidation() throws Exception {
    // Expecting JSON response with status SUCCESS
    var expectedJson = "{\"status\":\"SUCCESS\"}";
    mockMvc.perform(get("/mosaic/validate"))
      .andExpect(status().isOk())
      .andExpect(content().json(expectedJson));
  }
}
