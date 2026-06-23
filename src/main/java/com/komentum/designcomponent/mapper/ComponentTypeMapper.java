package com.komentum.designcomponent.mapper;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.dto.ComponentTypeCreateRequest;
import com.komentum.designcomponent.dto.ComponentTypeDto;
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
