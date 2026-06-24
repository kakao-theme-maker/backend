package com.komentum.global.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtils {

  private final ObjectMapper objectMapper;

  public <T> List<T> readListAsType(String path, Class<T> clazz) throws IOException {
    Resource resource = new ClassPathResource(path);
    CollectionType collectionType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, clazz);
    try (InputStream is = resource.getInputStream()) {
      return objectMapper.readValue(is, collectionType);
    }
  }

  public JsonNode readJsonNode(String path) throws IOException {
    Resource resource = new ClassPathResource(path);
    try (InputStream is = resource.getInputStream()) {
      return objectMapper.readTree(is);
    }
  }
}
