package com.komentum.designcomponent.mapper;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// DesignComponent Mapper
@Mapper(componentModel = "spring", uses = ComponentTypeMapper.class)
public interface DesignComponentMapper {

  @Mapping(target = "publicUserId", source = "user.publicUserId")
  @Mapping(target = "componentTypes", source = "componentTypes")
  DesignComponentDto toDto(DesignComponent designComponent);

  @Mapping(target = "designComponentId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "componentTypeMappings", ignore = true)
  @Mapping(target = "user", source = "user")
  @Mapping(target = "imageUrl", source = "imageUrl")
  DesignComponent toEntity(CreateDesignComponentRequest request, String imageUrl, User user);

}
