package org.folio.mosaic.util.error;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.folio.rest.acq.model.mosaic.MosaicCustomFields;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.CustomFields;

@UtilityClass
public class CustomFieldsUtil {

  private static final String VALUE = "value";

  public static CustomFields getCustomFieldsByEntityType(MosaicOrder mosaicOrder, MosaicCustomFields.EntityType entityType) {
    var convertedCustomFields = new CustomFields();
    mosaicOrder.getCustomFields().stream()
      .filter(field -> field.getEntityType() == entityType)
      .filter(field -> ObjectUtils.isNotEmpty(field.getAdditionalProperties()))
      .forEach(field -> {
        var value = field.getAdditionalProperties().get(VALUE);
        convertedCustomFields.withAdditionalProperty(field.getRefId(), value);
      });
    return convertedCustomFields;
  }
}
