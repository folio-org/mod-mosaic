package org.folio.mosaic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class FolioMosaicApplication {

  public static void main(String[] args) {
    SpringApplication.run(FolioMosaicApplication.class, args);
  }

}
