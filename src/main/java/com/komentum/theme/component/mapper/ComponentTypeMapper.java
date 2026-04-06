package com.komentum.theme.component.mapper;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.ComponentTypeCreateRequest;
import com.komentum.theme.component.dto.ComponentTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComponentTypeMapper {

  @Mapping(target = "componentTypeId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ComponentType toComponentType(ComponentTypeCreateRequest createDto);

  ComponentTypeDto toComponentTypeDto(ComponentType componentType);
}
