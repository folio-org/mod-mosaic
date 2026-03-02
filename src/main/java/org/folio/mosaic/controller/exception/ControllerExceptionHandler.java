package org.folio.mosaic.controller.exception;

import org.folio.mosaic.domain.dto.Errors;
import org.folio.mosaic.exception.ResourceAlreadyExistException;
import org.folio.mosaic.exception.ResourceNotFoundException;
import org.folio.mosaic.util.error.ErrorUtils;
import org.folio.mosaic.util.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class ControllerExceptionHandler {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(HttpClientErrorException.NotFound.class)
  public Errors handleNotFoundException(HttpClientErrorException.NotFound e) {
    logException(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.NOT_FOUND_ERROR);
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
  @ExceptionHandler(HttpClientErrorException.UnprocessableContent.class)
  public Errors handleValidationException(HttpClientErrorException.UnprocessableContent e) {
    logException(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.VALIDATION_ERROR);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpClientErrorException.class)
  public Errors handleClientException(HttpClientErrorException e) {
    logException(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.BAD_REQUEST_ERROR);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(HttpServerErrorException.class)
  public Errors handleServerException(HttpServerErrorException e) {
    logException(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.INTERNAL_ERROR);
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceNotFoundException.class)
  public Errors handleResourceNotFoundException(ResourceNotFoundException e) {
    logExceptionMessage(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.NOT_FOUND_ERROR);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ResourceAlreadyExistException.class)
  public Errors handleResourceAlreadyExistException(ResourceAlreadyExistException e) {
    logExceptionMessage(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.ALREADY_EXISTS_ERROR);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public Errors handleGenericException(Exception e) {
    logException(e);
    return ErrorUtils.getErrors(e.getMessage(), ErrorCode.UNKNOWN_ERROR);
  }

  public void logExceptionMessage(Exception e) {
    log.error("Exception occurred: {}", e.getMessage());
  }

  public void logException(HttpStatusCodeException e) {
    log.error("Feign exception occurred with status code {}", e.getStatusCode().value(), e);
  }

  public void logException(Exception e) {
    log.error("Generic exception occurred", e);
  }

}
