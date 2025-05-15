package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.mosaic.ReferenceNumberItem;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.Cost;
import org.folio.rest.acq.model.orders.FundDistribution;
import org.folio.rest.acq.model.orders.OrderFormat;
import org.folio.rest.acq.model.orders.PoLine;
import org.folio.rest.acq.model.orders.VendorDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CopilotGenerated(model = "Claude 3.7 Sonnet Thinking")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MosaicConverterTest {

  @Autowired
  private MosaicPoLineConverter mosaicPoLineConverter;

  @Autowired
  private MosaicOrderConverter mosaicOrderConverter;

  @Test
  void testConvertToCompositePurchaseOrderWithTemplate() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId)
      .withOrderType(CompositePurchaseOrder.OrderType.ONE_TIME)
      .withVendor("template-vendor")
      .withBillTo("template-billto")
      .withShipTo("template-shipto")
      .withAcqUnitIds(List.of("unit-1"))
      .withCustomFields(null);

    // Create a valid vendorDetail with reference numbers
    var vendorDetail = new org.folio.rest.acq.model.orders.VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withEdition("First Edition")
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Overridden Title")
      .withVendor("override-vendor")
      .withBillTo("override-billto")
      .withShipTo("override-shipto")
      .withAcqUnitIds(List.of("unit-override"))
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);

    assertNotNull(result);
    assertEquals(templateId, result.getTemplate());
    assertEquals("override-vendor", result.getVendor());
    assertEquals("override-billto", result.getBillTo());
    assertEquals("override-shipto", result.getShipTo());
    var resultPoLine = result.getPoLines().getFirst();
    assertEquals("Overridden Title", resultPoLine.getTitleOrPackage());
    assertEquals(List.of("unit-override"), result.getAcqUnitIds());
  }

  @Test
  void testOverrideTemplateValues() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId)
      .withVendor("template-vendor")
      .withBillTo("template-billto")
      .withShipTo("template-shipto");

    // Create a valid vendorDetail with reference numbers
    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("New Title")
      .withVendor("new-vendor")
      .withBillTo("new-billto")
      .withShipTo("new-shipto")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-456")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);

    assertEquals("new-vendor", result.getVendor());
    assertEquals("new-billto", result.getBillTo());
    assertEquals("new-shipto", result.getShipTo());
    var resultPoLine = result.getPoLines().getFirst();
    assertEquals("New Title", resultPoLine.getTitleOrPackage());
  }

  @Test
  void testOverrideCostValues() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    // Create a valid vendorDetail with reference numbers
    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Cost Override Test")
      .withListUnitPrice(10.0)
      .withCurrency("EUR")
      .withQuantityPhysical(5)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-456")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);

    var resultPoLine = result.getPoLines().getFirst();
    assertEquals("Cost Override Test", resultPoLine.getTitleOrPackage());
    assertEquals("EUR", resultPoLine.getCost().getCurrency());
    assertEquals(10.0, resultPoLine.getCost().getListUnitPrice());
    assertEquals(5, resultPoLine.getCost().getQuantityPhysical());
  }

  @Test
  void testOverrideOrderFields() {
    var templateId = UUID.randomUUID().toString();
    var orderId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId)
      .withVendor("template-vendor")
      .withBillTo("template-billto")
      .withShipTo("template-shipto");

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withId(orderId)
      .withTitle("Order Fields Test")
      .withVendor("override-vendor")
      .withBillTo("override-billto")
      .withShipTo("override-shipto")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);

    assertEquals(orderId, result.getId());
    assertEquals("override-vendor", result.getVendor());
    assertEquals("override-billto", result.getBillTo());
    assertEquals("override-shipto", result.getShipTo());
  }

  @Test
  void testOverrideCustomFields() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var customFields = new org.folio.rest.acq.model.mosaic.CustomFields();
    customFields.setAdditionalProperty("field1", "value1");
    customFields.setAdditionalProperty("field2", "value2");

    var mosaicOrder = new MosaicOrder()
      .withTitle("Custom Fields Test")
      .withCustomFields(customFields)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);

    assertNotNull(result.getCustomFields());
    assertEquals("value1", result.getCustomFields().getAdditionalProperties().get("field1"));
    assertEquals("value2", result.getCustomFields().getAdditionalProperties().get("field2"));
  }

  @Test
  void testOverridePoLineFields() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("PoLine Fields Test")
      .withPublicationDate("2023")
      .withEdition("Second Edition")
      .withRequesterName("Test Requester")
      .withSelectorName("Test Selector")
      .withNotes(List.of("Note 1", "Note 2"))
      .withPoLineDescription("Test PoLine Description")
      .withRenewalNote("Test Renewal Note")
      .withAcquisitionMethod("Purchase")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertEquals("2023", resultPoLine.getPublicationDate());
    assertEquals("Second Edition", resultPoLine.getEdition());
    assertEquals("Test Requester", resultPoLine.getRequester());
    assertEquals("Test Selector", resultPoLine.getSelector());
    assertEquals(List.of("Note 1", "Note 2"), result.getNotes());
    assertEquals("Test PoLine Description", resultPoLine.getPoLineDescription());
    assertEquals("Test Renewal Note", resultPoLine.getRenewalNote());
    assertEquals("Purchase", resultPoLine.getAcquisitionMethod());
  }

  @Test
  void testUpdatePoLineContributors() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var contributors = List.of(
      new org.folio.rest.acq.model.mosaic.Contributor()
        .withContributor("Author Name")
        .withContributorNameTypeId("2b94c631-fca9-4892-a730-03ee529ffe2a"),
      new org.folio.rest.acq.model.mosaic.Contributor()
        .withContributor("Editor Name")
        .withContributorNameTypeId("e8b311a6-3b21-43f2-a269-dd9310cb2d0a")
    );

    var mosaicOrder = new MosaicOrder()
      .withTitle("Contributors Test")
      .withContributors(contributors)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getContributors());
    assertEquals(2, resultPoLine.getContributors().size());
    assertEquals("Author Name", resultPoLine.getContributors().get(0).getContributor());
    assertEquals("2b94c631-fca9-4892-a730-03ee529ffe2a", resultPoLine.getContributors().get(0).getContributorNameTypeId());
    assertEquals("Editor Name", resultPoLine.getContributors().get(1).getContributor());
    assertEquals("e8b311a6-3b21-43f2-a269-dd9310cb2d0a", resultPoLine.getContributors().get(1).getContributorNameTypeId());
  }

  @Test
  void testOverrideElectronicFormat() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE) // Default is physical
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Electronic Format Test")
      .withFormat(MosaicOrder.Format.ELECTRONIC) // Override to electronic
      .withListUnitPriceElectronic(15.0)
      .withQuantityElectronic(3)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertEquals(OrderFormat.ELECTRONIC_RESOURCE, resultPoLine.getOrderFormat());
    assertEquals(15.0, resultPoLine.getCost().getListUnitPriceElectronic());
    assertEquals(3, resultPoLine.getCost().getQuantityElectronic());
    assertEquals(0, resultPoLine.getCost().getQuantityPhysical());
  }

  @Test
  void testOverridePEMixFormat() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("P/E Mix Format Test")
      .withFormat(MosaicOrder.Format.P_E_MIX) // P/E Mix format
      .withListUnitPrice(10.0)
      .withListUnitPriceElectronic(15.0)
      .withQuantityPhysical(2)
      .withQuantityElectronic(3)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertEquals(OrderFormat.P_E_MIX, resultPoLine.getOrderFormat());
    assertEquals(10.0, resultPoLine.getCost().getListUnitPrice());
    assertEquals(15.0, resultPoLine.getCost().getListUnitPriceElectronic());
    assertEquals(2, resultPoLine.getCost().getQuantityPhysical());
    assertEquals(3, resultPoLine.getCost().getQuantityElectronic());
  }

  @Test
  void testUpdatePoLineUserLimits() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(0)
        .withQuantityElectronic(1));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("User Limit Test")
      .withUserLimit("5")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getEresource());
    assertEquals("5", resultPoLine.getEresource().getUserLimit());
  }

  @Test
  void testUpdatePoLineMaterialTypeId() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var materialTypeId = UUID.randomUUID().toString();
    var materialSupplier = UUID.randomUUID().toString();

    var mosaicOrder = new MosaicOrder()
      .withTitle("Material Type Test")
      .withMaterialTypeId(materialTypeId)
      .withMaterialSupplier(materialSupplier)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getPhysical());
    assertEquals(materialTypeId, resultPoLine.getPhysical().getMaterialType());
    assertEquals(materialSupplier, resultPoLine.getPhysical().getMaterialSupplier());
  }

  @Test
  void testUpdatePoLineAccessProvider() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(0)
        .withQuantityElectronic(1));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var accessProvider = UUID.randomUUID().toString();

    var mosaicOrder = new MosaicOrder()
      .withTitle("Access Provider Test")
      .withAccessProvider(accessProvider)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getEresource());
    assertEquals(accessProvider, resultPoLine.getEresource().getAccessProvider());
  }

  @Test
  void testUpdatePoLineDetails() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(10.0)
        .withListUnitPriceElectronic(15.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    // Correctly create date objects using LocalDate and Date.from
    var fromDate = LocalDate.of(2023, 1, 1);
    var toDate = LocalDate.of(2023, 12, 31);

    var fromDateObj = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    var toDateObj = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    var details = new org.folio.rest.acq.model.mosaic.Details()
      .withIsAcknowledged(true)
      .withIsBinderyActive(true)
      .withReceivingNote("Test receiving note")
      .withSubscriptionFrom(fromDateObj)
      .withSubscriptionTo(toDateObj)
      .withSubscriptionInterval(365);

    var productIds = List.of(
      new org.folio.rest.acq.model.mosaic.ProductIdentifier()
        .withProductId("978-3-16-148410-0")
        .withProductIdType("ISBN")
    );
    details.setProductIds(productIds);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Details Test")
      .withDetails(details)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getDetails());
    assertEquals(true, resultPoLine.getDetails().getIsAcknowledged());
    assertEquals(true, resultPoLine.getDetails().getIsBinderyActive());
    assertEquals("Test receiving note", resultPoLine.getDetails().getReceivingNote());

    // For dates, compare with the same date objects used in input
    assertEquals(fromDateObj, resultPoLine.getDetails().getSubscriptionFrom());
    assertEquals(toDateObj, resultPoLine.getDetails().getSubscriptionTo());

    assertEquals(365, resultPoLine.getDetails().getSubscriptionInterval());
    assertNotNull(resultPoLine.getDetails().getProductIds());
    assertEquals(1, resultPoLine.getDetails().getProductIds().size());
    assertEquals("978-3-16-148410-0", resultPoLine.getDetails().getProductIds().getFirst().getProductId());
    assertEquals("ISBN", resultPoLine.getDetails().getProductIds().getFirst().getProductIdType());
  }

  @Test
  void testUpdatePoLineLocations() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var locationId = UUID.randomUUID().toString();
    var holdingId = UUID.randomUUID().toString();

    var locations = List.of(
      new org.folio.rest.acq.model.mosaic.Location()
        .withLocationId(locationId)
        .withQuantity(2)
        .withHoldingId(holdingId)
        .withTenantId("diku")
        .withQuantityPhysical(1)
        .withQuantityElectronic(1)
    );

    var mosaicOrder = new MosaicOrder()
      .withTitle("Locations Test")
      .withLocations(locations)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getLocations());
    assertEquals(1, resultPoLine.getLocations().size());
    assertEquals(locationId, resultPoLine.getLocations().getFirst().getLocationId());
    assertEquals(2, resultPoLine.getLocations().getFirst().getQuantity());
    assertEquals(holdingId, resultPoLine.getLocations().getFirst().getHoldingId());
    assertEquals("diku", resultPoLine.getLocations().getFirst().getTenantId());
    assertEquals(1, resultPoLine.getLocations().getFirst().getQuantityPhysical());
    assertEquals(1, resultPoLine.getLocations().getFirst().getQuantityElectronic());
  }

  @Test
  void testUpdatePoLineFunds() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var fundId = UUID.randomUUID().toString();

    var funds = List.of(
      new org.folio.rest.acq.model.mosaic.FundDistribution()
        .withFundId(fundId)
        .withDistributionType(org.folio.rest.acq.model.mosaic.FundDistribution.DistributionType.PERCENTAGE)
        .withValue(100.0)
    );

    var mosaicOrder = new MosaicOrder()
      .withTitle("Funds Test")
      .withFundDistribution(funds)
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    assertNotNull(resultPoLine.getFundDistribution());
    assertEquals(1, resultPoLine.getFundDistribution().size());
    assertEquals(fundId, resultPoLine.getFundDistribution().getFirst().getFundId());
    assertEquals(FundDistribution.DistributionType.PERCENTAGE, resultPoLine.getFundDistribution().getFirst().getDistributionType());
    assertEquals(100.0, resultPoLine.getFundDistribution().getFirst().getValue());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenCostIsNull() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(null);  // Setting cost to null to trigger validation failure

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL cost is empty after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenCurrencyIsInvalid() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("INVALID")  // Invalid currency code to trigger validation failure
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL currency is invalid after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenCurrencyIsEmpty() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("")  // Empty currency code to trigger validation failure
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL currency is empty after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenListUnitPriceIsInvalid() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(-1.0)  // Invalid price to trigger validation failure
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL list unit price physical is empty after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenListUnitPriceElectronicIsInvalid() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(-1.0)  // Invalid electronic price
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL list unit price electronic is empty after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenQuantityPhysicalIsInvalid() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)  // Physical resource
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(0)  // Invalid quantity for physical resource
        .withQuantityElectronic(1));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL quantity physical is 0 or less after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenQuantityElectronicIsInvalid() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE)  // Electronic resource
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));  // Invalid quantity for electronic resource

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL quantity electronic is 0 or less after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenPEMixQuantitiesAreInvalid() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.P_E_MIX)  // Physical/Electronic Mix
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(0)  // Invalid physical quantity for P_E_MIX
        .withQuantityElectronic(1));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Validation Test")
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL quantity P/E Mix is 0 or less after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenTitleIsEmpty() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("")  // Empty title
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("")  // Empty title in request too
      .withReferenceNumbers(List.of(
        new ReferenceNumberItem()
          .withRefNumber("mosaic-ref-123")
          .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER)
      ));

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL title or package is empty after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsThrowsExceptionWhenReferenceNumbersAreEmpty() {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var vendorDetail = new VendorDetail();
    // Empty reference numbers
    vendorDetail.setReferenceNumbers(List.of());

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    var mosaicOrder = new MosaicOrder()
      .withTitle("Reference Numbers Test")
      // No reference numbers in request
      .withReferenceNumbers(List.of());

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair));
    assertEquals("POL vendor reference numbers are empty after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsWithEmptyOrderFormat() {
    // Initialize a PoLine with null OrderFormat to test validation
    var poLine = new PoLine()
      .withTitleOrPackage("Test Title")
      .withOrderFormat(null)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(1));

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);
    poLine.setVendorDetail(vendorDetail);

    // Method should complete without throwing exceptions since quantities are valid
    // regardless of order format being null
    mosaicPoLineConverter.validatePoLineRequiredFields(poLine);

    // Assert order format is still null (validation doesn't modify it)
    assertNull(poLine.getOrderFormat());
  }

  @Test
  void testValidatePoLineRequiredFieldsWithOtherFormat() {
    // Test with OTHER format
    var poLine = new PoLine()
      .withTitleOrPackage("Test Title")
      .withOrderFormat(OrderFormat.OTHER)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(0)  // Can be zero for OTHER format
        .withQuantityElectronic(0));  // Can be zero for OTHER format

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);
    poLine.setVendorDetail(vendorDetail);

    // Should complete without exceptions as OTHER format doesn't have quantity requirements
    mosaicPoLineConverter.validatePoLineRequiredFields(poLine);

    // Assert order format remains OTHER
    assertEquals(OrderFormat.OTHER, poLine.getOrderFormat());
  }

  @Test
  void testValidatePoLineRequiredFieldsWithInvalidCurrencyCode() {
    // Test with invalid currency code (not just empty)
    var poLine = new PoLine()
      .withTitleOrPackage("Test Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("XYZ")  // Invalid currency code
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);
    poLine.setVendorDetail(vendorDetail);

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicPoLineConverter.validatePoLineRequiredFields(poLine));
    assertEquals("POL currency is invalid after the field was overridden from the request", exception.getMessage());
  }

  @Test
  void testValidatePoLineRequiredFieldsWithNullVendorDetail() {
    // Test with null VendorDetail
    var poLine = new PoLine()
      .withTitleOrPackage("Test Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(null)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    assertThrows(NullPointerException.class,
      () -> mosaicPoLineConverter.validatePoLineRequiredFields(poLine));
    // NullPointerException occurs when trying to access referenceNumbers on null vendorDetail
  }

  @Test
  void testValidatePoLineRequiredFieldsWithEmptyReferenceNumbers() {
    // Test with empty reference numbers list (not null)
    var poLine = new PoLine()
      .withTitleOrPackage("Test Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withListUnitPriceElectronic(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0));

    var vendorDetail = new VendorDetail();
    vendorDetail.setReferenceNumbers(List.of()); // Empty list
    poLine.setVendorDetail(vendorDetail);

    var exception = assertThrows(IllegalStateException.class,
      () -> mosaicPoLineConverter.validatePoLineRequiredFields(poLine));
    assertEquals("POL vendor reference numbers are empty after the field was overridden from the request", exception.getMessage());
  }
}
