package org.folio.mosaic.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.mosaic.client.OrganizationsClient;
import org.folio.mosaic.support.CopilotGenerated;
import org.folio.rest.acq.model.orgs.Organization;
import org.folio.rest.acq.model.orgs.OrganizationCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@CopilotGenerated(model = "Claude 4.0")
class OrganizationServiceTest {

  @Mock
  private OrganizationsClient organizationsClient;
  @InjectMocks
  private OrganizationService organizationService;

  @Test
  void testCreate() {
    // Given
    var organization = new Organization();
    organization.setId("org-123");
    organization.setCode("TEST_ORG");
    organization.setName("Test Organization");

    // When
    organizationService.create(organization);

    // Then
    verify(organizationsClient).createOrganization(organization);
  }

  @Test
  void testFindByCode_WhenOrganizationExists_ShouldReturnOrganization() {
    // Given
    var organizationCode = "MOSAIC";
    var expectedQuery = "code=MOSAIC";

    var organization = new Organization();
    organization.setId("org-456");
    organization.setCode(organizationCode);
    organization.setName("Mosaic Organization");

    var organizationCollection = new OrganizationCollection();
    organizationCollection.setOrganizations(List.of(organization));
    organizationCollection.setTotalRecords(1);

    when(organizationsClient.getOrganizations(expectedQuery)).thenReturn(organizationCollection);

    // When
    var result = organizationService.findByCode(organizationCode);

    // Then
    assertEquals(organization, result);
    assertEquals(organizationCode, result.getCode());
    verify(organizationsClient).getOrganizations(expectedQuery);
  }

  @Test
  void testFindByCode_WhenOrganizationDoesNotExist_ShouldReturnNull() {
    // Given
    var organizationCode = "NON_EXISTENT";
    var expectedQuery = "code=NON_EXISTENT";

    var organizationCollection = new OrganizationCollection();
    organizationCollection.setOrganizations(List.of()); // Empty list
    organizationCollection.setTotalRecords(0);

    when(organizationsClient.getOrganizations(expectedQuery)).thenReturn(organizationCollection);

    // When
    var result = organizationService.findByCode(organizationCode);

    // Then
    assertNull(result);
    verify(organizationsClient).getOrganizations(expectedQuery);
  }

  @Test
  void testFindByCode_WhenMultipleOrganizationsFound_ShouldReturnNull() {
    // Given
    var organizationCode = "DUPLICATE";
    var expectedQuery = "code=DUPLICATE";

    var organization1 = new Organization();
    organization1.setId("org-111");
    organization1.setCode(organizationCode);
    organization1.setName("First Duplicate Organization");

    var organization2 = new Organization();
    organization2.setId("org-222");
    organization2.setCode(organizationCode);
    organization2.setName("Second Duplicate Organization");

    var organizationCollection = new OrganizationCollection();
    organizationCollection.setOrganizations(List.of(organization1, organization2));
    organizationCollection.setTotalRecords(2);

    when(organizationsClient.getOrganizations(expectedQuery)).thenReturn(organizationCollection);

    // When
    var result = organizationService.findByCode(organizationCode);

    // Then
    assertNull(result);
    verify(organizationsClient).getOrganizations(expectedQuery);
  }

  @Test
  void testFindByCode_WithSpecialCharacters_ShouldFormatQueryCorrectly() {
    // Given
    var organizationCode = "ORG_WITH-SPECIAL.CHARS";
    var expectedQuery = "code=ORG_WITH-SPECIAL.CHARS";

    var organization = new Organization();
    organization.setId("org-special");
    organization.setCode(organizationCode);

    var organizationCollection = new OrganizationCollection();
    organizationCollection.setOrganizations(List.of(organization));
    organizationCollection.setTotalRecords(1);

    when(organizationsClient.getOrganizations(expectedQuery)).thenReturn(organizationCollection);

    // When
    var result = organizationService.findByCode(organizationCode);

    // Then
    assertEquals(organization, result);
    verify(organizationsClient).getOrganizations(expectedQuery);
  }
}
