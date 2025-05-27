package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.mosaic.Eresource;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.mosaic.Physical;
import org.folio.rest.acq.model.mosaic.ReferenceNumberItem;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.Cost;
import org.folio.rest.acq.model.orders.FundDistribution;
import org.folio.rest.acq.model.orders.OrderFormat;
import org.folio.rest.acq.model.orders.PoLine;
import org.folio.rest.acq.model.orders.VendorDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  @ParameterizedTest
  @MethodSource("testApplyOverridesWithWorkflowStatusArgs")
  void testApplyOverridesWithWorkflowStatus(MosaicOrder.WorkflowStatus mosaicStatus,
                                            CompositePurchaseOrder.WorkflowStatus expectedStatus,
                                            boolean shouldOverride) {
    // Given
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId)
      .withWorkflowStatus(CompositePurchaseOrder.WorkflowStatus.PENDING);

    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withCost(new Cost()
        .withListUnitPrice(1.0)
        .withCurrency("USD")
        .withQuantityPhysical(1));

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    // Create a MosaicOrder with the provided workflow status from parameter
    var mosaicOrder = new MosaicOrder()
      .withTitle("Workflow Status Test")
      .withWorkflowStatus(mosaicStatus);

    // When
    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);

    // Then
    assertEquals(expectedStatus, result.getWorkflowStatus());

    if (shouldOverride) {
      // Verify the status was overridden
      assertNotEquals(CompositePurchaseOrder.WorkflowStatus.PENDING, result.getWorkflowStatus());
    } else {
      // Verify the status was NOT overridden (kept template's value)
      assertEquals(CompositePurchaseOrder.WorkflowStatus.PENDING, result.getWorkflowStatus());
    }
  }

  // Method to provide all possible workflow status values, plus null case
  static Stream<Arguments> testApplyOverridesWithWorkflowStatusArgs() {
    return Stream.of(
      Arguments.of(MosaicOrder.WorkflowStatus.PENDING, CompositePurchaseOrder.WorkflowStatus.PENDING, false),
      Arguments.of(MosaicOrder.WorkflowStatus.OPEN, CompositePurchaseOrder.WorkflowStatus.OPEN, true),
      Arguments.of(MosaicOrder.WorkflowStatus.CLOSED, CompositePurchaseOrder.WorkflowStatus.CLOSED, true),
      Arguments.of(null, CompositePurchaseOrder.WorkflowStatus.PENDING, false)
    );
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
      .withFormat(MosaicOrder.OrderFormat.ELECTRONIC_RESOURCE) // Override to electronic
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
      .withFormat(MosaicOrder.OrderFormat.P_E_MIX) // P/E Mix format
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
  void testCheckinItemsOverride() {
    // Case 1: Override false to true
    var templateWithFalseCheckinItems = createPoLineTemplate(false);
    var mosaicOrderWithTrueCheckinItems = new MosaicOrder()
      .withTitle("CheckinItems Test - False to True")
      .withCheckinItems(true);

    testCheckinItemsOverride(templateWithFalseCheckinItems, mosaicOrderWithTrueCheckinItems, true);

    // Case 2: Override true to false
    var templateWithTrueCheckinItems = createPoLineTemplate(true);
    var mosaicOrderWithFalseCheckinItems = new MosaicOrder()
      .withTitle("CheckinItems Test - True to False")
      .withCheckinItems(false);

    testCheckinItemsOverride(templateWithTrueCheckinItems, mosaicOrderWithFalseCheckinItems, false);

    // Case 3: No override (null in MosaicOrder)
    var templateWithDefaultCheckinItems = createPoLineTemplate(false);
    var mosaicOrderWithNullCheckinItems = new MosaicOrder()
      .withTitle("CheckinItems Test - No Override, null in MosaicOrder");

    testCheckinItemsOverride(templateWithDefaultCheckinItems, mosaicOrderWithNullCheckinItems, false);
  }

  // Physical

  @Test
  void testPhysicalMapper_updatePoLinePhysical_complete() {
    // Setup test data with all physical fields
    var poLine = new PoLine()
      .withTitleOrPackage("Complete Physical Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE);

    var materialTypeId = UUID.randomUUID().toString();
    var materialSupplier = UUID.randomUUID().toString();

    var mosaicOrder = new MosaicOrder()
      .withTitle("Complete Physical Mapping Test")
      .withPhysical(new Physical()
        .withMaterialType(materialTypeId)
        .withMaterialSupplier(materialSupplier)
        .withCreateInventory(Physical.CreateInventory.INSTANCE_HOLDING_ITEM));

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify all fields were properly mapped
    assertNotNull(poLine.getPhysical());
    assertEquals(materialTypeId, poLine.getPhysical().getMaterialType());
    assertEquals(materialSupplier, poLine.getPhysical().getMaterialSupplier());
    assertEquals(org.folio.rest.acq.model.orders.Physical.CreateInventory.INSTANCE_HOLDING_ITEM,
      poLine.getPhysical().getCreateInventory());
  }

  @Test
  void testPhysicalMapper_updatePoLinePhysical_withNullPhysical() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("Null Physical Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE);

    // Create a MosaicOrder with null physical
    var mosaicOrder = new MosaicOrder()
      .withTitle("Null Physical Test");

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify nothing changed since physical was null
    assertNull(poLine.getPhysical());
  }

  @Test
  void testPhysicalMapper_updatePoLinePhysical_withExistingPhysical() {
    // Setup test data with existing physical
    var existingMaterialType = UUID.randomUUID().toString();
    var existingPhysical = new org.folio.rest.acq.model.orders.Physical()
      .withMaterialType(existingMaterialType)
      .withCreateInventory(org.folio.rest.acq.model.orders.Physical.CreateInventory.INSTANCE);

    var poLine = new PoLine()
      .withTitleOrPackage("Existing Physical Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withPhysical(existingPhysical);

    // New values to override
    var materialSupplier = UUID.randomUUID().toString();

    var mosaicOrder = new MosaicOrder()
      .withTitle("Existing Physical Test")
      .withPhysical(new Physical()
        .withMaterialSupplier(materialSupplier));

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify the existing materialType was preserved and new materialSupplier was added
    assertNotNull(poLine.getPhysical());
    assertEquals(existingMaterialType, poLine.getPhysical().getMaterialType());
    assertEquals(materialSupplier, poLine.getPhysical().getMaterialSupplier());
    assertEquals(org.folio.rest.acq.model.orders.Physical.CreateInventory.INSTANCE,
      poLine.getPhysical().getCreateInventory());
  }

  @Test
  void testPhysicalMapper_updateCreateInventory() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("CreateInventory Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE);

    // Create a MosaicOrder with only createInventory
    var mosaicOrder = new MosaicOrder()
      .withTitle("CreateInventory Test")
      .withPhysical(new Physical()
        .withCreateInventory(Physical.CreateInventory.INSTANCE));

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify only createInventory was set
    assertNotNull(poLine.getPhysical());
    assertNull(poLine.getPhysical().getMaterialType());
    assertNull(poLine.getPhysical().getMaterialSupplier());
    assertEquals(org.folio.rest.acq.model.orders.Physical.CreateInventory.INSTANCE,
      poLine.getPhysical().getCreateInventory());
  }

  @Test
  void testPhysicalMapper_updateMaterialType() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("MaterialType Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE);

    var materialTypeId = UUID.randomUUID().toString();

    // Create a MosaicOrder with only materialType
    var mosaicOrder = new MosaicOrder()
      .withTitle("MaterialType Test")
      .withPhysical(new Physical()
        .withMaterialType(materialTypeId));

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify only materialType was set
    assertNotNull(poLine.getPhysical());
    assertEquals(materialTypeId, poLine.getPhysical().getMaterialType());
    assertNull(poLine.getPhysical().getMaterialSupplier());
    assertNull(poLine.getPhysical().getCreateInventory());
  }

  @Test
  void testPhysicalMapper_updateMaterialSupplier() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("MaterialSupplier Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE);

    var materialSupplier = UUID.randomUUID().toString();

    // Create a MosaicOrder with only materialSupplier
    var mosaicOrder = new MosaicOrder()
      .withTitle("MaterialSupplier Test")
      .withPhysical(new Physical()
        .withMaterialSupplier(materialSupplier));

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify only materialSupplier was set
    assertNotNull(poLine.getPhysical());
    assertNull(poLine.getPhysical().getMaterialType());
    assertEquals(materialSupplier, poLine.getPhysical().getMaterialSupplier());
    assertNull(poLine.getPhysical().getCreateInventory());
  }

  @Test
  void testPhysicalMapper_withEmptyPhysical() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("Empty Physical Test")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE);

    // Create a MosaicOrder with empty physical object
    var mosaicOrder = new MosaicOrder()
      .withTitle("Empty Physical Test")
      .withPhysical(new Physical());

    // Create PhysicalMapper and apply mappings
    var physicalMapper = new PhysicalMapper();
    physicalMapper.updatePoLinePhysical(mosaicOrder, poLine);

    // Verify an empty physical object was created
    assertNotNull(poLine.getPhysical());
    assertNull(poLine.getPhysical().getMaterialType());
    assertNull(poLine.getPhysical().getMaterialSupplier());
    assertNull(poLine.getPhysical().getCreateInventory());
  }

  // Electronic

  @Test
  void testCreatePoLineFromTemplate_withElectronicResource() {
    // Setup test data with an electronic resource format
    var poLineTemplate = new PoLine()
      .withTitleOrPackage("Electronic Resource Title")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE)
      .withVendorDetail(new VendorDetail())
      .withCost(new Cost()
        .withListUnitPriceElectronic(15.0)
        .withCurrency("USD")
        .withQuantityElectronic(1));

    // Create poLine from template
    var result = mosaicPoLineConverter.createPoLineFromTemplate(poLineTemplate);

    // Verify electronic resource fields were properly mapped
    assertNotNull(result);
    assertEquals(OrderFormat.ELECTRONIC_RESOURCE, result.getOrderFormat());
    assertNotNull(result.getCost());
    assertEquals(15.0, result.getCost().getListUnitPriceElectronic());
  }

  @Test
  void testElectronicMapper_updatePoLineEResource_complete() {
    // Setup test data with all electronic fields
    var poLine = new PoLine()
      .withTitleOrPackage("Complete Electronic Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    var accessProvider = UUID.randomUUID().toString();
    var materialTypeId = UUID.randomUUID().toString();
    var userLimit = "10";

    var mosaicOrder = new MosaicOrder()
      .withTitle("Complete Electronic Mapping Test")
      .withEresource(new Eresource()
        .withAccessProvider(accessProvider)
        .withMaterialType(materialTypeId)
        .withUserLimit(userLimit)
        .withCreateInventory(Eresource.CreateInventory.INSTANCE_HOLDING));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify all fields were properly mapped
    assertNotNull(poLine.getEresource());
    assertEquals(accessProvider, poLine.getEresource().getAccessProvider());
    assertEquals(materialTypeId, poLine.getEresource().getMaterialType());
    assertEquals(userLimit, poLine.getEresource().getUserLimit());
    assertEquals(org.folio.rest.acq.model.orders.Eresource.CreateInventory.INSTANCE_HOLDING,
      poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_updatePoLineEResource_withNullEresource() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("Null Electronic Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    // Create a MosaicOrder with null eresource
    var mosaicOrder = new MosaicOrder()
      .withTitle("Null Electronic Test");

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify nothing changed since eresource was null
    assertNull(poLine.getEresource());
  }

  @Test
  void testElectronicMapper_updatePoLineEResource_withExistingEresource() {
    // Setup test data with existing eresource
    var existingAccessProvider = UUID.randomUUID().toString();
    var existingEresource = new org.folio.rest.acq.model.orders.Eresource()
      .withAccessProvider(existingAccessProvider)
      .withCreateInventory(org.folio.rest.acq.model.orders.Eresource.CreateInventory.INSTANCE);

    var poLine = new PoLine()
      .withTitleOrPackage("Existing Electronic Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE)
      .withEresource(existingEresource);

    // New values to override
    var newMaterialType = UUID.randomUUID().toString();
    var userLimit = "5";

    var mosaicOrder = new MosaicOrder()
      .withTitle("Existing Electronic Test")
      .withEresource(new Eresource()
        .withMaterialType(newMaterialType)
        .withUserLimit(userLimit));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify the existing values were preserved and new ones applied
    assertNotNull(poLine.getEresource());
    assertEquals(existingAccessProvider, poLine.getEresource().getAccessProvider());
    assertEquals(newMaterialType, poLine.getEresource().getMaterialType());
    assertEquals(userLimit, poLine.getEresource().getUserLimit());
    assertEquals(org.folio.rest.acq.model.orders.Eresource.CreateInventory.INSTANCE,
      poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_updateCreateInventory() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("CreateInventory Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    // Create a MosaicOrder with only createInventory
    var mosaicOrder = new MosaicOrder()
      .withTitle("CreateInventory Test")
      .withEresource(new Eresource()
        .withCreateInventory(Eresource.CreateInventory.INSTANCE));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify only createInventory was set
    assertNotNull(poLine.getEresource());
    assertNull(poLine.getEresource().getMaterialType());
    assertNull(poLine.getEresource().getAccessProvider());
    assertNull(poLine.getEresource().getUserLimit());
    assertEquals(org.folio.rest.acq.model.orders.Eresource.CreateInventory.INSTANCE,
      poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_updateMaterialType() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("MaterialType Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    var materialTypeId = UUID.randomUUID().toString();

    // Create a MosaicOrder with only materialType
    var mosaicOrder = new MosaicOrder()
      .withTitle("MaterialType Test")
      .withEresource(new Eresource()
        .withMaterialType(materialTypeId));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify only materialType was set
    assertNotNull(poLine.getEresource());
    assertEquals(materialTypeId, poLine.getEresource().getMaterialType());
    assertNull(poLine.getEresource().getAccessProvider());
    assertNull(poLine.getEresource().getUserLimit());
    assertNull(poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_updateAccessProvider() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("AccessProvider Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    var accessProvider = UUID.randomUUID().toString();

    // Create a MosaicOrder with only accessProvider
    var mosaicOrder = new MosaicOrder()
      .withTitle("AccessProvider Test")
      .withEresource(new Eresource()
        .withAccessProvider(accessProvider));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify only accessProvider was set
    assertNotNull(poLine.getEresource());
    assertNull(poLine.getEresource().getMaterialType());
    assertEquals(accessProvider, poLine.getEresource().getAccessProvider());
    assertNull(poLine.getEresource().getUserLimit());
    assertNull(poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_updateUserLimit() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("UserLimit Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    var userLimit = "15";

    // Create a MosaicOrder with only userLimit
    var mosaicOrder = new MosaicOrder()
      .withTitle("UserLimit Test")
      .withEresource(new Eresource()
        .withUserLimit(userLimit));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify only userLimit was set
    assertNotNull(poLine.getEresource());
    assertNull(poLine.getEresource().getMaterialType());
    assertNull(poLine.getEresource().getAccessProvider());
    assertEquals(userLimit, poLine.getEresource().getUserLimit());
    assertNull(poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_withEmptyEresource() {
    // Setup test data
    var poLine = new PoLine()
      .withTitleOrPackage("Empty Electronic Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

    // Create a MosaicOrder with empty eresource object
    var mosaicOrder = new MosaicOrder()
      .withTitle("Empty Electronic Test")
      .withEresource(new Eresource());

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify an empty eresource object was created
    assertNotNull(poLine.getEresource());
    assertNull(poLine.getEresource().getMaterialType());
    assertNull(poLine.getEresource().getAccessProvider());
    assertNull(poLine.getEresource().getUserLimit());
    assertNull(poLine.getEresource().getCreateInventory());
  }

  @Test
  void testElectronicMapper_withAllCreateInventoryValues() {
    // Test all possible CreateInventory enum values
    var createInventoryValues = new Eresource.CreateInventory[] {
      Eresource.CreateInventory.INSTANCE,
      Eresource.CreateInventory.INSTANCE_HOLDING,
      Eresource.CreateInventory.NONE
    };

    for (var inventoryValue : createInventoryValues) {
      var poLine = new PoLine()
        .withTitleOrPackage("CreateInventory Value Test")
        .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE);

      var mosaicOrder = new MosaicOrder()
        .withTitle("CreateInventory Value Test")
        .withEresource(new Eresource()
          .withCreateInventory(inventoryValue));

      var electronicMapper = new ElectronicMapper();
      electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

      assertNotNull(poLine.getEresource());
      var expectedCreateInventory = org.folio.rest.acq.model.orders.Eresource.CreateInventory.fromValue(inventoryValue.value());
      assertEquals(expectedCreateInventory, poLine.getEresource().getCreateInventory());
    }
  }

  @Test
  void testElectronicMapper_withNullCreateInventory() {
    // Setup test data with null createInventory
    var poLine = new PoLine()
      .withTitleOrPackage("Null CreateInventory Test")
      .withOrderFormat(OrderFormat.ELECTRONIC_RESOURCE)
      .withEresource(new org.folio.rest.acq.model.orders.Eresource()
        .withCreateInventory(org.folio.rest.acq.model.orders.Eresource.CreateInventory.INSTANCE));

    var mosaicOrder = new MosaicOrder()
      .withTitle("Null CreateInventory Test")
      .withEresource(new Eresource()
        .withMaterialType(UUID.randomUUID().toString())
        .withCreateInventory(null));

    // Create ElectronicMapper and apply mappings
    var electronicMapper = new ElectronicMapper();
    electronicMapper.updatePoLineEResource(mosaicOrder, poLine);

    // Verify createInventory remains unchanged when null is provided
    assertNotNull(poLine.getEresource());
    assertNotNull(poLine.getEresource().getMaterialType());
    assertEquals(org.folio.rest.acq.model.orders.Eresource.CreateInventory.INSTANCE,
      poLine.getEresource().getCreateInventory());
  }

  private PoLine createPoLineTemplate(boolean checkinItemsValue) {
    var vendorDetail = new VendorDetail();
    var referenceNumbers = List.of(new org.folio.rest.acq.model.orders.ReferenceNumberItem()
      .withRefNumber("ref-123")
      .withRefNumberType(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER));
    vendorDetail.setReferenceNumbers(referenceNumbers);

    return new PoLine()
      .withTitleOrPackage("Default Title")
      .withOrderFormat(OrderFormat.PHYSICAL_RESOURCE)
      .withVendorDetail(vendorDetail)
      .withCost(new Cost()
        .withListUnitPrice(10.0)
        .withCurrency("USD")
        .withQuantityPhysical(1)
        .withQuantityElectronic(0))
      .withCheckinItems(checkinItemsValue);
  }

  private void testCheckinItemsOverride(PoLine poLineTemplate, MosaicOrder mosaicOrder, boolean expectedValue) {
    var templateId = UUID.randomUUID().toString();
    var orderTemplate = new CompositePurchaseOrder()
      .withId(templateId);

    var templatePair = Pair.of(orderTemplate, poLineTemplate);

    // Convert to CompositePurchaseOrder
    var result = mosaicOrderConverter.convertToCompositePurchaseOrder(mosaicOrder, templatePair);
    var resultPoLine = result.getPoLines().getFirst();

    // Verify that checkinItems value matches expected
    assertNotNull(resultPoLine);
    assertEquals(expectedValue, resultPoLine.getCheckinItems());

    // For cases where we're actually overriding, verify the value changed
    if (mosaicOrder.getCheckinItems() != null) {
      assertEquals(mosaicOrder.getCheckinItems(), resultPoLine.getCheckinItems());
    } else {
      // For null case, verify template value was preserved
      assertEquals(poLineTemplate.getCheckinItems(), resultPoLine.getCheckinItems());
    }
  }
}
