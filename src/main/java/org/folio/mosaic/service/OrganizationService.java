package org.folio.mosaic.service;

import lombok.RequiredArgsConstructor;
import org.folio.mosaic.client.OrganizationsClient;
import org.folio.rest.acq.model.orgs.Organization;
import org.folio.rest.acq.model.orgs.OrganizationCollection;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrganizationService {

  private final OrganizationsClient organizationsClient;

  public void create(Organization organization) {
    organizationsClient.createOrganization(organization);
  }

  public Organization findByCode(String organizationCode) {
    String query = String.format("code=%s", organizationCode);
    OrganizationCollection organizations = organizationsClient.getOrganizations(query);
    if (organizations.getOrganizations().size() == 1) {
      return organizations.getOrganizations().getFirst();
    }
    return null;
  }
}
