package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComponentTypeRequest {

  private String explain;

  private Platform platform;

  private String componentPath;

  private String componentName;

  private Integer sizeX;

  private Integer sizeY;
}