package org.folio.mosaic.config;

import lombok.extern.log4j.Log4j2;
import org.folio.mosaic.client.OrdersClient;
import org.folio.mosaic.client.OrganizationsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Log4j2
public class HttpClientConfiguration {

  @Bean
  public OrdersClient ordersClient(HttpServiceProxyFactory factory) {
    return factory.createClient(OrdersClient.class);
  }

  @Bean
  public OrganizationsClient organizationsClient(HttpServiceProxyFactory factory) {
    return factory.createClient(OrganizationsClient.class);
  }

}
