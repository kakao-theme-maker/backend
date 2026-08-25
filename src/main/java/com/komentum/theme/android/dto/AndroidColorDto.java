package com.komentum.theme.android.dto;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.theme.core.domain.ThemeStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AndroidColorDto {

  String color;
  String attrName;

  public static AndroidColorDto fromEntity(ThemeStyle themeStyle,
      PlatformColorStyle platformColorStyle) {
    return AndroidColorDto.builder()
        .color(themeStyle.getColorWithAlphaArgb())
        .attrName(platformColorStyle.getResourceName())
        .build();
  }
}
