package org.folio.mosaic.util.error;

import lombok.experimental.UtilityClass;
import org.folio.rest.acq.model.mosaic.MosaicCustomFields;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.CustomFields;

@UtilityClass
public class CustomFieldsUtil {

  public static CustomFields getCustomFieldsByEntityType(MosaicOrder mosaicOrder, MosaicCustomFields.EntityType entityType) {
    var convertedCustomFields = new CustomFields();
    mosaicOrder.getCustomFields().stream()
      .filter(field -> field.getEntityType() == entityType)
      .forEach(customField -> convertedCustomFields.withAdditionalProperty(customField.getRefId(), customField.getValue()));
    return convertedCustomFields;
  }
}
