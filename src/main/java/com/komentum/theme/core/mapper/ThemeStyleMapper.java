package com.komentum.theme.core.mapper;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeStyleDto;
import com.komentum.theme.core.dto.ThemeStyleRequest;
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
