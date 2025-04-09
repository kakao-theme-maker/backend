package com.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponentDto {

    private Integer designComponentId;
    private String userEmail;
    private ComponentTypeDto componentType; // ComponentType 정보를 포함
    private String imageUrl;
    private String rgba;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPublic;
    // 내부 ComponentType DTO
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
