package com.komentum.theme.core.dto;

import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.utils.RegexValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import java.util.Set;
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

  @Schema(description = "변경할 이미지 정보에 대한 typeCode:이미지 정보 맵, 이미지 파일은 typeCode 이름의 part로 전달한다. 사용 가능한 typeCode는 Schemas의 TypeCode 참고")
  Map<TypeCode, ThemeImageUpdateRequest> typeCodes;

  @Schema(description = "변경할 색상에 대한 styleCode:색상 맵. 사용 가능한 styleCode는 Schemas의 StyleCode 참고")
  Map<StyleCode, ThemeStyleUpdateRequest> styleCodes;

  @Schema(description = "제거할 이미지의 typeCode 목록")
  Set<TypeCode> deleteTypeCodes;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "수정할 이미지 정보")
  public static class ThemeImageUpdateRequest {

    @Schema(description = "이미지 inset 정보 ( 말풍선인 경우 ), nullable")
    InsetUpdateDto inset;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "말풍선 inset 정보")
  public static class InsetUpdateDto {

    @Schema(description = "말풍선 컨텐츠 padding")
    int top;
    @Schema(description = "말풍선 컨텐츠 padding")
    int left;
    @Schema(description = "말풍선 컨텐츠 padding")
    int bottom;
    @Schema(description = "말풍선 컨텐츠 padding")
    int right;
    @Schema(description = "말풍선이 확장되기 시작하는 이미지 내 x 좌표")
    int stretchX;
    @Schema(description = "말풍선이 확장되기 시작하는 이미지 내 y 좌표")
    int stretchY;
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "테마 색상 갱신 DTO")
  public static class ThemeStyleUpdateRequest {

    @NotNull
    @Pattern(regexp = RegexValidator.HEX_COLOR_REGEX, message = "color must be a valid hex color")
    @Schema(description = "새로 저장할 hex color")
    String color;

    @NotNull
    @Min(0)
    @Max(100)
    Integer alpha;
  }
}
