package org.folio.mosaic.support;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.folio.mosaic.FolioMosaicApplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class JsonUtils {

  private static final JsonMapper MAPPER = JsonMapper.builder()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .serializationInclusion(JsonInclude.Include.NON_NULL)
    .addModule(new JavaTimeModule())
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
