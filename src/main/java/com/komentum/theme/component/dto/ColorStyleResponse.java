package com.komentum.theme.component.dto;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorStyleResponse {

  private Integer colorStyleId;
  private String explain;
  private Platform platform;
  private String styleSheetPath;
  private String styleElementName;
  private String stylePropsName;

  public static ColorStyleResponse from(ColorStyle colorStyle) {
    return ColorStyleResponse.builder()
        .colorStyleId(colorStyle.getColorStyleId())
        .explain(colorStyle.getExplain())
        .platform(colorStyle.getPlatform())
        .styleSheetPath(colorStyle.getStyleSheetPath())
        .styleElementName(colorStyle.getStyleElementName())
        .stylePropsName(colorStyle.getStylePropsName())
        .build();
  }
}