package org.folio.mosaic.exception;

/**
 * Exception thrown when template initialization operations fail.
 * This occurs when default templates cannot be loaded from configuration files
 * or when template creation in the system fails.
 */
public class TemplateInitializationException extends RuntimeException {

  public TemplateInitializationException(String message) {
    super(message);
  }

  public TemplateInitializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
