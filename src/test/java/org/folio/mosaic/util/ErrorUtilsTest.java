package org.folio.mosaic.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.folio.mosaic.CopilotGenerated;
import org.folio.mosaic.util.error.ErrorCode;
import org.junit.jupiter.api.Test;

import lombok.val;

@CopilotGenerated(partiallyGenerated = true)
class ErrorUtilsTest {

  @Test
  void testGetErrors() {
    val errors = ErrorUtils.getErrors(ErrorCode.VALIDATION_ERROR.toError(), ErrorCode.BAD_REQUEST_ERROR.toError());

    assertThat(errors).isNotNull();
    assertThat(errors.getErrors()).isNotNull().isNotEmpty().hasSize(2);
    assertThat(errors.getTotalRecords()).isEqualTo(2);
    assertThat(errors.getErrors().get(0).getMessage()).isEqualTo(ErrorCode.VALIDATION_ERROR.getDescription());
    assertThat(errors.getErrors().get(1).getMessage()).isEqualTo(ErrorCode.BAD_REQUEST_ERROR.getDescription());
  }

  @Test
  void testGetErrorsEmpty() {
    val errors = ErrorUtils.getErrors();

    assertThat(errors).isNotNull();
    assertThat(errors.getErrors()).isNotNull().isEmpty();
    assertThat(errors.getTotalRecords()).isZero();
  }

  @Test
  void testGetErrorsWithMessage() {
    val errorMessage = "Custom message";

    val errors = ErrorUtils.getErrors(errorMessage, ErrorCode.UNKNOWN_ERROR);

    assertThat(errors).isNotNull();
    assertThat(errors.getErrors()).isNotNull().isNotEmpty().hasSize(1);
    assertThat(errors.getTotalRecords()).isEqualTo(1);
    assertThat(errors.getErrors().getFirst().getMessage()).isEqualTo(errorMessage);
  }
}
