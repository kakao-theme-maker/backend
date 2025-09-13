package com.komentum.theme.component.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponentDto {

  private Integer designComponentId;
  private String userEmail;
  private ComponentTypeDto componentType;
  private String imageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean isPublic;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ComponentTypeDto {

    private Integer componentTypeId;
    private String explain;
    private String iosComponentPath;
    private String iosComponentName;
    private String androidComponentPath;
    private String androidComponentName;
    private Integer sizeX;
    private Integer sizeY;
  }
}