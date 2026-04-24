package com.komentum.theme.theme.mapper;

import com.komentum.theme.component.dto.CreateThemeRequest;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
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
  ThemeComponent convertToTransientEntity(CreateThemeRequest request, String versionNumber);
}
