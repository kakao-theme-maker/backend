package com.komentum.theme.core.dto;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
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
    PlatformScope platformScope;

    public static TypeCodeInfo of(DesignComponent designComponent, PlatformScope platformScope) {
      return TypeCodeInfo.builder()
          .designComponentId(designComponent.getDesignComponentId())
          .imageUrl(designComponent.getImageUrl())
          .platformScope(platformScope)
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
    Integer alpha;
    PlatformScope platformScope;

    public static StyleCodeInfo of(String color, Integer alpha, PlatformScope platformScope) {
      return StyleCodeInfo.builder()
          .color(color)
          .alpha(alpha)
          .platformScope(platformScope)
          .build();
    }
  }

  private Integer themeComponentId;
  private String themeName;
  Map<TypeCode, TypeCodeInfo> typeCodes;
  Map<StyleCode, StyleCodeInfo> styleCodes;
}
