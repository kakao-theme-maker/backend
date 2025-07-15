package com.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentTypeDto {
    private Integer componentTypeId;
    private String explain;
    private String iosComponentPath;
    private String iosComponentName;
    private String androidComponentPath;
    private String androidComponentName;
    private Integer sizeX;
    private Integer sizeY;
}

