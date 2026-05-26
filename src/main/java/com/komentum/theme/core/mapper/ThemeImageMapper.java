package com.komentum.theme.core.mapper;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.dto.ThemeImageDto;
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
