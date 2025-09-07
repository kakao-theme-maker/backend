package com.komentum.theme.theme.mapper;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.theme.theme.dto.ThemeStyleDto;
import com.komentum.theme.theme.dto.ThemeStyleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ThemeStyleMapper {

  @Mapping(target = "colorStyleId", source = "colorStyle.colorStyleId")
  ThemeStyleDto convertToDto(ThemeStyle themeStyle);

  @Mapping(target = "themeStyleId", ignore = true)
  @Mapping(target = "themeComponent", ignore = true)
  ThemeStyle convertToTransientEntity(ThemeStyleRequest themeStyleRequest, ColorStyle colorStyle);
}
