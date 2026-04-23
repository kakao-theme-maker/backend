package com.komentum.theme.theme.dto;

import com.komentum.theme.theme.domain.ThemeComponent;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ThemePreviewDto {

  private final Integer themeComponentId;
  private final String previewImageUrl;
  private final String themeName;
  private final LocalDateTime createdAt;
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
