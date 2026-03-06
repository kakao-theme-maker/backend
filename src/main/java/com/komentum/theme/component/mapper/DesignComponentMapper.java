package com.komentum.theme.component.mapper;

import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// DesignComponent Mapper
@Mapper(componentModel = "spring")
public interface DesignComponentMapper {

  @Mapping(target = "publicUserId", source = "user.publicUserId")
  DesignComponentDto toDto(DesignComponent designComponent);

  @Mapping(target = "designComponentId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "user", source = "user")
  DesignComponent toEntity(CreateDesignComponentRequest request, User user);

}
