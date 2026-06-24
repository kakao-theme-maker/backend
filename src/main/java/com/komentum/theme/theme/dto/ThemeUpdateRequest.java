package com.komentum.theme.theme.dto;

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
