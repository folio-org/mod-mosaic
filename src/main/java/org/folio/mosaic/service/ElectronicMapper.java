package org.folio.mosaic.service;

import org.apache.commons.lang3.ObjectUtils;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.orders.Eresource;
import org.folio.rest.acq.model.orders.PoLine;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
public class ElectronicMapper {

  public void updatePoLineEResource(MosaicOrder mosaicOrder, PoLine poLine) {
    if (ObjectUtils.isEmpty(mosaicOrder.getEresource())) {
      return;
    }
    var eresource = poLine.getEresource() != null ? poLine.getEresource() : new Eresource();

    updatePoLineCreateInventory(mosaicOrder, eresource);
    updatePoLineMaterialType(mosaicOrder, eresource);
    updatePoLineAccessProvider(mosaicOrder, eresource);
    updatePoLineUserLimits(mosaicOrder, eresource);
    poLine.setEresource(eresource);
  }

  private void updatePoLineCreateInventory(MosaicOrder mosaicOrder, Eresource eresource) {
    if (ObjectUtils.isEmpty(mosaicOrder.getEresource().getCreateInventory())) {
      return;
    }
    var createInventory = mosaicOrder.getEresource().getCreateInventory().name();

    eresource.setCreateInventory(Eresource.CreateInventory.valueOf(createInventory));
  }

  private void updatePoLineMaterialType(MosaicOrder mosaicOrder, Eresource eresource) {
    if (isBlank(mosaicOrder.getEresource().getMaterialType())) {
      return;
    }
    eresource.setMaterialType(mosaicOrder.getEresource().getMaterialType());
  }

  private void updatePoLineAccessProvider(MosaicOrder mosaicOrder, Eresource eresource) {
    if (isBlank(mosaicOrder.getEresource().getAccessProvider())) {
      return;
    }
    eresource.setAccessProvider(mosaicOrder.getEresource().getAccessProvider());
  }

  private void updatePoLineUserLimits(MosaicOrder mosaicOrder, Eresource eresource) {
    if (isBlank(mosaicOrder.getEresource().getUserLimit())) {
      return;
    }
    eresource.setUserLimit(mosaicOrder.getEresource().getUserLimit());
  }
}
