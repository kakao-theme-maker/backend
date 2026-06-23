package com.komentum.theme.core.mapper;

import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.CreateThemeRequest;
import com.komentum.theme.core.dto.ThemeComponentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ThemeStyleMapper.class, ThemeImageMapper.class})
public interface ThemeComponentMapper {

  @Mapping(target = "previewImageUrl", ignore = true)
  @Mapping(target = "images", source = "component.themeImages")
  @Mapping(target = "styles", source = "component.themeStyles")
  ThemeComponentDto convertToDto(ThemeComponent component);

  @Mapping(target = "themeComponentId", ignore = true)
  @Mapping(target = "isDone", ignore = true)
  @Mapping(target = "themeImages", ignore = true)
  @Mapping(target = "themeStyles", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ThemeComponent convertToTransientEntity(CreateThemeRequest request, String versionNumber);
}
