package com.komentum.designcomponent.mapper;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.dto.ColorStyleCreateDto;
import com.komentum.designcomponent.dto.ColorStyleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ColorStyleMapper {

  @Mapping(target = "colorStyleId", ignore = true)
  ColorStyle toColorStyle(ColorStyleCreateDto createDto);

  ColorStyleResponse toColorStyleResponse(ColorStyle colorStyle);
}
