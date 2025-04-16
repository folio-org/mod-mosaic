package org.folio.mosaic.exception;

public class ResourceAlreadyExistException extends RuntimeException {

  private static final String RESOURCE_EXIST_MSG_TEMPLATE = "Resource of type '%s' already exists";
  private static final String RESOURCE_ID_TEMPLATE = "%s' with id: '%s";

  public ResourceAlreadyExistException(Class<?> cls, String id) {
    super(RESOURCE_EXIST_MSG_TEMPLATE.formatted(RESOURCE_ID_TEMPLATE.formatted(cls.getSimpleName(), id)));
  }

  public ResourceAlreadyExistException(Class<?> cls) {
    super(RESOURCE_EXIST_MSG_TEMPLATE.formatted(cls.getSimpleName()));
  }

}
