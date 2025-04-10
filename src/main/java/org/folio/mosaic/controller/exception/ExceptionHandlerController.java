package org.folio.mosaic.controller.exception;

import org.folio.mosaic.domain.dto.Errors;
import org.folio.mosaic.util.ErrorUtils;
import org.folio.mosaic.util.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;
import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class ExceptionHandlerController {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(FeignException.NotFound.class)
  public Errors handleNotFoundException(FeignException.NotFound e) {
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.NOT_FOUND_ERROR);
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(FeignException.UnprocessableEntity.class)
  public Errors handleNotFoundException(FeignException.UnprocessableEntity e) {
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.VALIDATION_ERROR);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(FeignException.FeignClientException.class)
  public Errors handleNotFoundException(FeignException.FeignClientException e) {
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.BAD_REQUEST_ERROR);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(FeignException.FeignServerException.class)
  public Errors handleNotFoundException(FeignException.FeignServerException e) {
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.INTERNAL_ERROR);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public Errors handleNotFoundException(Exception e) {
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.UNKNOWN_ERROR);
  }

}
