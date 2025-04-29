package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.mosaic.CustomFields;
import org.folio.rest.acq.model.mosaic.Details;
import org.folio.rest.acq.model.mosaic.FundDistribution;
import org.folio.rest.acq.model.mosaic.Location;
import org.folio.rest.acq.model.mosaic.MosaicOrder;
import org.folio.rest.acq.model.mosaic.ProductIdentifier;
import org.folio.rest.acq.model.mosaic.ReferenceNumberItem;
import org.folio.rest.acq.model.orders.CompositePurchaseOrder;
import org.folio.rest.acq.model.orders.OrderFormat;
import org.folio.rest.acq.model.orders.PoLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@CopilotGenerated
@ExtendWith(MockitoExtension.class)
class MosaicOrderConverterTest {

  @InjectMocks
  private MosaicOrderConverter converter;

  @Test
  void testConvertToCompositePurchaseOrderWithNullTemplate() {
    // Given
    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Test Book")
      .withAuthor("Test Author")
      .withPublicationDate("2023")
      .withEdition("First Edition");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();
    assertEquals("Test Book", poLine.getTitleOrPackage());
    assertEquals("Test Author", poLine.getContributors().getFirst().getContributor());
    assertEquals("2023", poLine.getPublicationDate());
    assertEquals("First Edition", poLine.getEdition());
  }

  @Test
  void testConvertToCompositePurchaseOrderWithTemplate() {
    // Given
    String templateId = UUID.randomUUID().toString();
    CompositePurchaseOrder template = new CompositePurchaseOrder()
      .withId(templateId)
      .withOrderType(CompositePurchaseOrder.OrderType.ONE_TIME)
      .withVendor("vendor-id")
      .withBillTo("bill-to-id")
      .withShipTo("ship-to-id");

    List<String> acqUnitIds = new ArrayList<>();
    acqUnitIds.add("acq-unit-1");
    template.setAcqUnitIds(acqUnitIds);

    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Test Book")
      .withAuthor("Test Author");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, template);

    // Then
    assertNotNull(result);
    assertEquals(templateId, result.getTemplate());
    assertEquals(CompositePurchaseOrder.OrderType.ONE_TIME, result.getOrderType());
    assertEquals("vendor-id", result.getVendor());
    assertEquals("bill-to-id", result.getBillTo());
    assertEquals("ship-to-id", result.getShipTo());
    assertEquals(acqUnitIds, result.getAcqUnitIds());

    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();
    assertEquals("Test Book", poLine.getTitleOrPackage());
    assertEquals("Test Author", poLine.getContributors().getFirst().getContributor());
  }

  @Test
  void testConvertPhysicalResource() {
    // Given
    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Physical Book")
      .withListUnitPrice(29.99)
      .withQuantityPhysical(3)
      .withFormat(MosaicOrder.Format.PHYSICAL)
      .withCurrency("USD")
      .withMaterialTypeId("material-type-id");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();
    assertEquals(OrderFormat.PHYSICAL_RESOURCE, poLine.getOrderFormat());
    assertEquals(29.99, poLine.getCost().getListUnitPrice());
    assertEquals(3, poLine.getCost().getQuantityPhysical());
    assertEquals(0, poLine.getCost().getQuantityElectronic());
    assertEquals("USD", poLine.getCost().getCurrency());
    assertNotNull(poLine.getPhysical());
    assertEquals("material-type-id", poLine.getPhysical().getMaterialType());
  }

  @Test
  void testConvertElectronicResource() {
    // Given
    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("E-Book")
      .withListUnitPriceElectronic(19.99)
      .withQuantityElectronic(5)
      .withFormat(MosaicOrder.Format.ELECTRONIC)
      .withCurrency("EUR")
      .withUserLimit("10")
      .withAccessProvider("access-provider-id");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();
    assertEquals(OrderFormat.ELECTRONIC_RESOURCE, poLine.getOrderFormat());
    assertEquals(19.99, poLine.getCost().getListUnitPriceElectronic());
    assertEquals(5, poLine.getCost().getQuantityElectronic());
    assertEquals(0, poLine.getCost().getQuantityPhysical());
    assertEquals("EUR", poLine.getCost().getCurrency());
    assertNotNull(poLine.getEresource());
    assertEquals("10", poLine.getEresource().getUserLimit());
    assertEquals("access-provider-id", poLine.getEresource().getAccessProvider());
  }

  @Test
  void testConvertMixedResource() {
    // Given
    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Mixed Resource")
      .withListUnitPrice(24.99)
      .withListUnitPriceElectronic(14.99)
      .withQuantityPhysical(2)
      .withQuantityElectronic(3)
      .withFormat(MosaicOrder.Format.P_E_MIX)
      .withCurrency("GBP");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();
    assertEquals(OrderFormat.P_E_MIX, poLine.getOrderFormat());
    assertEquals(24.99, poLine.getCost().getListUnitPrice());
    assertEquals(14.99, poLine.getCost().getListUnitPriceElectronic());
    assertEquals(2, poLine.getCost().getQuantityPhysical());
    assertEquals(3, poLine.getCost().getQuantityElectronic());
    assertEquals("GBP", poLine.getCost().getCurrency());
  }

  @Test
  void testConvertOrderWithDetails() {
    // Given
    ProductIdentifier productId = new ProductIdentifier()
      .withProductId("9781234567890")
      .withProductIdType("ISBN");

    List<ProductIdentifier> productIds = new ArrayList<>();
    productIds.add(productId);

    Details details = new Details()
      .withIsAcknowledged(true)
      .withIsBinderyActive(false)
      .withReceivingNote("Test receiving note")
      .withSubscriptionFrom(new Date())
      .withSubscriptionTo(new Date())
      .withSubscriptionInterval(365)
      .withProductIds(productIds);

    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Book with Details")
      .withDetails(details);

    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();

    assertNotNull(poLine.getDetails());
    assertEquals(true, poLine.getDetails().getIsAcknowledged());
    assertEquals(false, poLine.getDetails().getIsBinderyActive());
    assertEquals("Test receiving note", poLine.getDetails().getReceivingNote());
    assertNotNull(poLine.getDetails().getSubscriptionFrom());
    assertNotNull(poLine.getDetails().getSubscriptionTo());
    assertEquals(365, poLine.getDetails().getSubscriptionInterval());

    assertEquals(1, poLine.getDetails().getProductIds().size());
    assertEquals("9781234567890", poLine.getDetails().getProductIds().getFirst().getProductId());
    assertEquals("ISBN", poLine.getDetails().getProductIds().getFirst().getProductIdType());
  }

  @Test
  void testConvertOrderWithReceivingNote() {
    // Given
    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Book with Note")
      .withReceivingNote("Special handling required");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();
    assertNotNull(poLine.getDetails());
    assertEquals("Special handling required", poLine.getDetails().getReceivingNote());
  }

  @Test
  void testConvertOrderWithReferenceNumbers() {
    // Given
    String vendorId = UUID.randomUUID().toString();
    ReferenceNumberItem refNumber = new ReferenceNumberItem()
      .withRefNumber("REF-123")
      .withRefNumberType(ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER);

    List<ReferenceNumberItem> referenceNumbers = new ArrayList<>();
    referenceNumbers.add(refNumber);

    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Book with Ref Numbers")
      .withVendor(vendorId)
      .withReferenceNumbers(referenceNumbers);

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(vendorId, result.getVendor());
    assertEquals(1, result.getPoLines().size());

    PoLine poLine = result.getPoLines().getFirst();
    assertNotNull(poLine.getVendorDetail());
    assertEquals(1, poLine.getVendorDetail().getReferenceNumbers().size());
    assertEquals("REF-123", poLine.getVendorDetail().getReferenceNumbers().getFirst().getRefNumber());
    assertEquals(org.folio.rest.acq.model.orders.ReferenceNumberItem.RefNumberType.VENDOR_CONTINUATION_REFERENCE_NUMBER,
                poLine.getVendorDetail().getReferenceNumbers().getFirst().getRefNumberType());
  }

  @Test
  void testConvertOrderWithFundDistribution() {
    // Given
    FundDistribution fundDist = new FundDistribution()
      .withFundId("fund-id-1")
      .withDistributionType(FundDistribution.DistributionType.PERCENTAGE)
      .withValue(100.0);

    List<FundDistribution> fundDistributions = new ArrayList<>();
    fundDistributions.add(fundDist);

    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Book with Fund")
      .withFundDistribution(fundDistributions);

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());

    PoLine poLine = result.getPoLines().getFirst();
    assertEquals(1, poLine.getFundDistribution().size());
    assertEquals("fund-id-1", poLine.getFundDistribution().getFirst().getFundId());
    assertEquals(org.folio.rest.acq.model.orders.FundDistribution.DistributionType.PERCENTAGE,
                poLine.getFundDistribution().getFirst().getDistributionType());
    assertEquals(100.0, poLine.getFundDistribution().getFirst().getValue());
  }

  @Test
  void testConvertOrderWithLocations() {
    // Given
    Location location = new Location()
      .withLocationId("location-id-1")
      .withQuantity(5)
      .withHoldingId("holding-id-1")
      .withTenantId("tenant-id-1")
      .withQuantityPhysical(3)
      .withQuantityElectronic(2);

    List<Location> locations = new ArrayList<>();
    locations.add(location);

    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Book with Locations")
      .withLocations(locations);

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());

    PoLine poLine = result.getPoLines().getFirst();
    assertEquals(1, poLine.getLocations().size());
    assertEquals("location-id-1", poLine.getLocations().getFirst().getLocationId());
    assertEquals(5, poLine.getLocations().getFirst().getQuantity());
    assertEquals("holding-id-1", poLine.getLocations().getFirst().getHoldingId());
    assertEquals("tenant-id-1", poLine.getLocations().getFirst().getTenantId());
    assertEquals(3, poLine.getLocations().getFirst().getQuantityPhysical());
    assertEquals(2, poLine.getLocations().getFirst().getQuantityElectronic());
  }

  @Test
  void testConvertOrderWithCustomFields() {
    // Given
    CustomFields customFields = new CustomFields();
    customFields.withAdditionalProperty("customField1", "value1");
    customFields.withAdditionalProperty("customField2", "value2");

    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Book with Custom Fields")
      .withCustomFields(customFields);

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertNotNull(result.getCustomFields());
    assertEquals("value1", result.getCustomFields().getAdditionalProperties().get("customField1"));
    assertEquals("value2", result.getCustomFields().getAdditionalProperties().get("customField2"));
  }

  @Test
  void testConvertOrderWithAllMetadata() {
    // Given
    MosaicOrder mosaicOrder = new MosaicOrder()
      .withTitle("Complete Book")
      .withAuthor("Test Author")
      .withRequesterName("John Requester")
      .withSelectorName("Jane Selector")
      .withPoLineNote("Important note")
      .withPoLineDescription("Detailed description")
      .withRenewalNote("Renewal instructions");

    // When
    CompositePurchaseOrder result = converter.convertToCompositePurchaseOrder(mosaicOrder, null);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getPoLines().size());
    PoLine poLine = result.getPoLines().getFirst();

    assertEquals("John Requester", poLine.getRequester());
    assertEquals("Jane Selector", poLine.getSelector());
    assertEquals("Detailed description", poLine.getPoLineDescription());
    assertEquals("Renewal instructions", poLine.getRenewalNote());

    assertEquals(1, result.getNotes().size());
    assertEquals("Important note", result.getNotes().getFirst());
  }
}
