package com.komentum.theme.theme.mapper;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.dto.ThemeImageDto;
import org.springframework.stereotype.Component;

@Component
public class ThemeImageMapper {

  public ThemeImageDto convertToDto(ThemeImage themeImage) {
    return ThemeImageDto.builder()
        .designComponentId(themeImage.getDesignComponent().getDesignComponentId())
        .build();
  }

  public ThemeImage convertToTransientEntity(ComponentType componentType,
      DesignComponent designComponent) {
    return ThemeImage.builder()
        .designComponent(designComponent)
        .componentType(componentType)
        .build();
  }
}
