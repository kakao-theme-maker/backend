package com.komentum.theme.theme.dto;

import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.StyleCode;
import com.komentum.theme.component.enums.TypeCode;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeDetailResponse {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TypeCodeInfo {

    Integer designComponentId;
    String imageUrl;

    public static TypeCodeInfo of(DesignComponent designComponent) {
      return TypeCodeInfo.builder()
          .designComponentId(designComponent.getDesignComponentId())
          .imageUrl(designComponent.getImageUrl())
          .build();
    }
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StyleCodeInfo {

    String color;

    public static StyleCodeInfo of(String color) {
      return StyleCodeInfo.builder().color(color).build();
    }
  }

  private Integer themeComponentId;
  private String themeName;
  Map<TypeCode, TypeCodeInfo> typeCodes;
  Map<StyleCode, StyleCodeInfo> styleCodes;
}
