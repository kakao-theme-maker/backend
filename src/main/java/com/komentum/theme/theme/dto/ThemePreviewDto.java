package com.komentum.theme.theme.dto;

import com.komentum.theme.theme.domain.ThemeComponent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "테마 미리보기 응답 DTO")
public class ThemePreviewDto {

  @Schema(description = "테마 식별자")
  private final Integer themeComponentId;
  @Schema(description = "테마 대표 이미지")
  private final String previewImageUrl;
  @Schema(description = "테마 이름")
  private final String themeName;
  @Schema(description = "테마 생성일")
  private final LocalDateTime createdAt;
  @Schema(description = "테마 갱신일")
  private final LocalDateTime updatedAt;

  public static ThemePreviewDto from(ThemeComponent themeComponent, String previewImageUrl) {
    return ThemePreviewDto.builder()
        .themeComponentId(themeComponent.getThemeComponentId())
        .previewImageUrl(previewImageUrl)
        .themeName(themeComponent.getThemeName())
        .createdAt(themeComponent.getCreatedAt())
        .updatedAt(themeComponent.getUpdatedAt())
        .build();
  }
}
