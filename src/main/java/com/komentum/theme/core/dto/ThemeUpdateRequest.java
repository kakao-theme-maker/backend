package com.komentum.theme.core.dto;

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
public class ThemeUpdateRequest {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeImageUpdateRequest {

    Integer designComponentId;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeStyleUpdateRequest {

    String color;
  }

  String themeName;
  Map<TypeCode, ThemeImageUpdateRequest> typeCodes;
  Map<StyleCode, ThemeStyleUpdateRequest> styleCodes;
}
