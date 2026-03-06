package org.folio.mosaic.client;

import org.folio.rest.acq.model.orgs.Organization;
import org.folio.rest.acq.model.orgs.OrganizationCollection;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("organizations")
public interface OrganizationsClient {

  @GetExchange(value = "/organizations")
  OrganizationCollection getOrganizations(@RequestParam("query") String query);

  @PostExchange(value = "/organizations")
  void createOrganization(Organization organization);
}
