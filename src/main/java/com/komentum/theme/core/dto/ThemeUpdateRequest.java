package com.komentum.theme.core.dto;

import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.utils.RegexValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @NotNull
    @Pattern(regexp = RegexValidator.HEX_COLOR_REGEX, message = "color must be a valid hex color")
    String color;

    @NotNull
    @Min(0)
    @Max(100)
    Integer alpha;
  }

  String themeName;
  Map<TypeCode, ThemeImageUpdateRequest> typeCodes;
  @Valid
  Map<StyleCode, ThemeStyleUpdateRequest> styleCodes;
}
