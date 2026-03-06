package org.folio.mosaic.support;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.folio.mosaic.FolioMosaicApplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class JsonUtils {

  private static final ObjectMapper MAPPER = JsonMapper.builder()
    .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
    .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(JsonInclude.Include.NON_NULL))
    .build();

  @SneakyThrows
  public static String getMockData(String path) {
    try (InputStream resourceAsStream = FolioMosaicApplication.class.getClassLoader().getResourceAsStream(path)) {
      if (resourceAsStream != null) {
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }

  @SneakyThrows
  public static <T> T getMockData(String path, Class<T> clazz) {
    return MAPPER.readValue(getMockData(path), clazz);
  }

  @SneakyThrows
  public static <T> T toObject(String json, Class<T> entityClass) {
    return MAPPER.readValue(json, entityClass);
  }

  @SneakyThrows
  public static <T> String toJson(T object) {
    return MAPPER.writeValueAsString(object);
  }

}
