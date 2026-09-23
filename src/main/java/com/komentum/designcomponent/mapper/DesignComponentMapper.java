package com.komentum.designcomponent.mapper;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.global.utils.FileManager;
import com.komentum.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

// DesignComponent Mapper
@Mapper(componentModel = "spring", uses = ComponentTypeMapper.class)
public abstract class DesignComponentMapper {

  @Autowired
  protected FileManager fileManager;

  @Mapping(target = "publicUserId", source = "user.publicUserId")
  @Mapping(target = "componentTypes", source = "componentTypes")
  @Mapping(target = "imageUrl", source = "fileName", qualifiedByName = "fileNameToUrl")
  public abstract DesignComponentDto toDto(DesignComponent designComponent);

  @Mapping(target = "designComponentId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "componentTypeMappings", ignore = true)
  @Mapping(target = "user", source = "user")
  @Mapping(target = "fileName", source = "fileName")
  public abstract DesignComponent toEntity(CreateDesignComponentRequest request, String fileName,
      User user);

  /**
   * 저장된 파일명을 FileManager를 통해 파일 URL로 변환한다. 파일명이 없으면 null을 반환한다.
   */
  @Named("fileNameToUrl")
  protected String fileNameToUrl(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return null;
    }
    return fileManager.resolveFilePath(fileName);
  }
}
