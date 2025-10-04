package com.komentum.theme.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlEditor {

  @Getter
  @Builder
  @AllArgsConstructor
  public static class YamlData {

    Yaml yaml;
    Map<String, Object> yamlMap;
  }

  public YamlData loadYamlMap(Path yamlPath) throws IOException {
    if (!yamlPath.getFileName().toString().matches(".*\\.(yaml|yml)$")) {
      throw new RuntimeException("[YamlEditor] invalid yaml extension");
    }
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setPrettyFlow(true);
    Yaml yaml = new Yaml(options);
    Map<String, Object> yamlMap = yaml.load(new FileReader(yamlPath.toString()));
    return YamlData.builder()
        .yaml(yaml)
        .yamlMap(yamlMap).build();
  }

  public Map<String, Object> getYamlValueMap(YamlData data, String key) {
    Map<String, Object> yamlMap = data.getYamlMap();
    if (!yamlMap.containsKey(key) || !(yamlMap.get(key) instanceof Map)) {
      throw new RuntimeException("[YamlEditor] key is invalid or value is not map");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> yamlValueMap = (Map<String, Object>) yamlMap.get(key);
    return yamlValueMap;
  }

  public void writeYaml(Path yamlPath, YamlData data) throws IOException {
    try (FileWriter fileWriter = new FileWriter(yamlPath.toString())) {
      data.getYaml().dump(data.getYamlMap(), fileWriter);
    }
  }
}
