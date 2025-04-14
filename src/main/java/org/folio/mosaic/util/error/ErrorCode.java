package org.folio.mosaic.util.error;

import org.folio.mosaic.domain.dto.Error;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {

  VALIDATION_ERROR("validationError", "Validation error"),
  NOT_FOUND_ERROR("notFoundError", "Resource not found"),
  INTERNAL_ERROR("internalError", "Internal error occurred"),
  BAD_REQUEST_ERROR("badRequestError", "Bad request sent by the client"),
  UNKNOWN_ERROR("unknownError", "Unknown error occurred");

  private final String code;
  private final String description;

  public Error toError() {
    return new Error().code(code).message(description);
  }

}
