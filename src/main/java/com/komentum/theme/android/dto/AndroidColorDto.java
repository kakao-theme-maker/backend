package com.komentum.theme.android.dto;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.theme.domain.ThemeStyle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AndroidColorDto {

  String sheetPath;
  String color;
  String attrName;

  /**
   * TODO: 추후 별도 커밋에서 개선할 예정
   * */
  @Deprecated
  public static AndroidColorDto fromEntity(ThemeStyle themeStyle) {
    ColorStyle colorStyle = themeStyle.getColorStyle();
    return AndroidColorDto.builder()
        .color(themeStyle.getColor())
//        .attrName(colorStyle.getStylePropsName())
//        .sheetPath(colorStyle.getStyleSheetPath())
        .build();
  }
}
