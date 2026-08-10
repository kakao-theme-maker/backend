package com.komentum.theme.core.dto;

import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "테마 수정 요청 DTO")
public class ThemeUpdateRequest {

  @Schema(description = "변경할 테마의 이름")
  String themeName;

  @Schema(description = "변경할 이미지에 대한 typeCode:이미지 맵")
  Map<TypeCode, ThemeImageUpdateRequest> typeCodes;

  @Schema(description = "변경할 색상에 대한 styleCode:색상 맵")
  Map<StyleCode, ThemeStyleUpdateRequest> styleCodes;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "수정할 이미지 정보")
  public static class ThemeImageUpdateRequest {

    Integer designComponentId;
    Inset inset;
  }

  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Inset {

    int top;
    int left;
    int bottom;
    int right;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeStyleUpdateRequest {

    String color;
  }
}
