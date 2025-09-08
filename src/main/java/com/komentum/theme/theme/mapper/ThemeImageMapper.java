package com.komentum.theme.theme.mapper;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.dto.ThemeImageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ThemeImageMapper {

  @Mapping(source = "designComponent.designComponentId", target = "designComponentId")
  ThemeImageDto convertToDto(ThemeImage themeImage);

  @Mapping(target = "themeImageId", ignore = true)
  @Mapping(target = "themeComponent", ignore = true)
  @Mapping(target = "designComponent", source = "designComponent")
  @Mapping(target = "componentType", source = "componentType")
  ThemeImage convertToTransientEntity(ComponentType componentType, DesignComponent designComponent);
}
