package org.folio.mosaic.exception;

public class ResourceNotFoundException extends RuntimeException {

  private static final String RESOURCE_EXIST_MSG_TEMPLATE = "Resource of type '%s' is not found";
  private static final String RESOURCE_ID_TEMPLATE = "%s' with id: '%s";

  public ResourceNotFoundException(Class<?> cls, String id) {
    super(RESOURCE_EXIST_MSG_TEMPLATE.formatted(RESOURCE_ID_TEMPLATE.formatted(cls.getSimpleName(), id)));
  }

  public ResourceNotFoundException(Class<?> cls) {
    super(RESOURCE_EXIST_MSG_TEMPLATE.formatted(cls.getSimpleName()));
  }

}
