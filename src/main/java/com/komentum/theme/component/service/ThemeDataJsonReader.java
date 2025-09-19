package com.komentum.theme.component.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.enums.Platform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeDataJsonReader {

  private final ObjectMapper objectMapper;

  private static class JsonComponentType {

    @JsonProperty("component_path")
    String componentPath;
    @JsonProperty("component_name")
    String componentName;
  }

  private static class JsonColorStyle {

    @JsonProperty("explain")
    String explain;
    @JsonProperty("platform")
    String platform;
    @JsonProperty("sheet_style_path")
    String sheetStylePath;
    @JsonProperty("sheet_element_name")
    String sheetElementName;
    @JsonProperty("sheet_props_name")
    String sheetPropsName;
  }

  private static final String COMPONENT_TYPE_JSON_PATH = "src/main/resources/theme-data/component_type.json";
  private static final String COLOR_STYLE_JSON_PATH = "src/main/resources/theme-data/color_style.json";

  private <T> List<T> readThemeDataList(String pathString, Class<T> clazz) throws IOException {
    return objectMapper.readValue(new File(pathString),
        objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
  }

  public List<ComponentType> readJsonComponentTypes() throws IOException {
    List<ComponentType> componentTypes = new ArrayList<>();
    readThemeDataList(COMPONENT_TYPE_JSON_PATH, JsonComponentType.class).forEach(
        jsonComponentType -> {
          ComponentType componentType = ComponentType.builder()
              .platform(Platform.ANDROID)
              .componentPath(jsonComponentType.componentPath)
              .componentName(jsonComponentType.componentName).build();
          componentTypes.add(componentType);
        });
    return componentTypes;
  }

  public List<ColorStyle> readJsonColorStyles() throws IOException {
    List<ColorStyle> colorStyles = new ArrayList<>();
    readThemeDataList(COLOR_STYLE_JSON_PATH, JsonColorStyle.class).forEach(jsonColorStyle -> {
      ColorStyle colorStyle = ColorStyle.builder()
          .explain(jsonColorStyle.explain)
          .stylePropsName(jsonColorStyle.sheetPropsName)
          .styleElementName(jsonColorStyle.sheetElementName)
          .styleSheetPath(jsonColorStyle.sheetStylePath)
          .platform(Platform.fromString(jsonColorStyle.platform))
          .build();
      colorStyles.add(colorStyle);
    });
    return colorStyles;
  }
}
