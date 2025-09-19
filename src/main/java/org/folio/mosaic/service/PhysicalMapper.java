package org.folio.mosaic.service;

import org.apache.commons.lang3.ObjectUtils;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.CompositePoLine;
import org.folio.rest.acq.model.orders.Physical;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class PhysicalMapper {

  public void updatePoLinePhysical(MosaicOrder mosaicOrder, CompositePoLine poLine) {
    if (ObjectUtils.isEmpty(mosaicOrder.getPhysical())) {
      return;
    }
    var physical = poLine.getPhysical() != null ? poLine.getPhysical() : new Physical();

    updatePoLineCreateInventory(mosaicOrder, physical);
    updatePoLineMaterialType(mosaicOrder, physical);
    updatePoLineMaterialSupplier(mosaicOrder, physical);
    poLine.setPhysical(physical);
  }

  private void updatePoLineCreateInventory(MosaicOrder mosaicOrder, Physical physical) {
    if (ObjectUtils.isEmpty(mosaicOrder.getPhysical().getCreateInventory())) {
      return;
    }
    var createInventory = mosaicOrder.getPhysical().getCreateInventory().name();

    physical.setCreateInventory(Physical.CreateInventory.valueOf(createInventory));
  }

  private void updatePoLineMaterialType(MosaicOrder mosaicOrder, Physical physical) {
    if (isBlank(mosaicOrder.getPhysical().getMaterialType())) {
      return;
    }
    physical.setMaterialType(mosaicOrder.getPhysical().getMaterialType());
  }

  private void updatePoLineMaterialSupplier(MosaicOrder mosaicOrder, Physical physical) {
    if (isBlank(mosaicOrder.getPhysical().getMaterialSupplier())) {
      return;
    }
    physical.setMaterialSupplier(mosaicOrder.getPhysical().getMaterialSupplier());
  }
}
