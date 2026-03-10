package com.komentum.theme.component.mapper;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.ColorStyleCreateDto;
import com.komentum.theme.component.dto.ColorStyleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorStyleMapper {

  @Mapping(target = "colorStyleId", ignore = true)
  ColorStyle toColorStyle(ColorStyleCreateDto createDto);

  ColorStyleResponse toColorStyleResponse(ColorStyle colorStyle);
}
