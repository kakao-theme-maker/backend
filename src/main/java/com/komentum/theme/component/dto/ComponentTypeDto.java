package com.komentum.theme.component.dto;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentTypeDto {

  private Integer componentTypeId;
  private String explain;
  private Platform platform;
  private String componentPath;
  private String componentName;
  private Integer sizeX;
  private Integer sizeY;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static ComponentTypeDto from(ComponentType componentType) {
    return ComponentTypeDto.builder()
        .componentTypeId(componentType.getComponentTypeId())
        .explain(componentType.getExplain())
        .platform(componentType.getPlatform())
        .componentPath(componentType.getComponentPath())
        .componentName(componentType.getComponentName())
        .sizeX(componentType.getSizeX())
        .sizeY(componentType.getSizeY())
        .createdAt(componentType.getCreatedAt())
        .updatedAt(componentType.getUpdatedAt())
        .build();
  }
}

