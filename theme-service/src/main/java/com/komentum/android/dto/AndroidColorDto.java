package com.komentum.android.dto;

import com.komentum.component.domain.ColorStyle;
import com.komentum.theme.domain.ThemeStyle;
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

  public static AndroidColorDto fromEntity(ThemeStyle themeStyle) {
    ColorStyle colorStyle = themeStyle.getColorStyle();
    return AndroidColorDto.builder()
        .color(themeStyle.getColor())
        .attrName(colorStyle.getAndroidStyleName())
        .build();
  }
}
