package com.komentum.theme.theme.mapper;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.theme.theme.dto.ThemeStyleDto;
import com.komentum.theme.theme.dto.ThemeStyleRequest;
import org.springframework.stereotype.Component;

@Component
public class ThemeStyleMapper {

  public ThemeStyleDto convertToDto(ThemeStyle themeStyle) {
    ColorStyle colorStyle = themeStyle.getColorStyle();
    return ThemeStyleDto.builder()
        .colorTypeId(colorStyle.getColorTypeId())
        .styleElementName(colorStyle.getStyleElementName())
        .stylePropsName(colorStyle.getStylePropsName())
        .color(themeStyle.getColor())
        .explain(colorStyle.getExplain())
        .build();
  }

  public ThemeStyle convertToTransientEntity(ThemeStyleRequest themeStyleRequest,
      ColorStyle colorStyle) {
    return ThemeStyle.builder()
        .colorStyle(colorStyle)
        .color(themeStyleRequest.getColor())
        .build();
  }
}
