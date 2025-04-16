package org.folio.mosaic.util.error;

import java.util.List;

import org.folio.mosaic.domain.dto.Error;
import org.folio.mosaic.domain.dto.Errors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorUtils {

  public static Errors getErrors(String message, ErrorCode errorCode) {
    return getErrors(errorCode.toError().message(message));
  }

  public static Errors getErrors(Error... errors) {
    return new Errors().errors(List.of(errors)).totalRecords(errors.length);
  }

}
