package org.folio.mosaic.client;

import org.folio.rest.acq.model.orgs.Organization;
import org.folio.rest.acq.model.orgs.OrganizationCollection;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("organizations")
public interface OrganizationsClient {

  @GetMapping(value = "/organizations")
  OrganizationCollection getOrganizations(@RequestParam("query") String query);

  @PostMapping(value = "/organizations")
  void createOrganization(Organization organization);
}
