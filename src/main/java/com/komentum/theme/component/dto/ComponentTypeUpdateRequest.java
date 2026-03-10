package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "component type 갱신 요청용 DTO")
public class ComponentTypeUpdateRequest {

  @Schema(description = "component type 설명, null 허용")
  private String explain;

  @Schema(description = "component type 플랫폼, null 허용", example = "ANDROID | IOS")
  private Platform platform;

  @Schema(description = "component type 경로, null 허용")
  private String componentPath;

  @Schema(description = "component type 이름, null 허용")
  private String componentName;

  @Schema(description = "component type x축 크기, null 허용")
  private Integer sizeX;

  @Schema(description = "component type y축 크기, null 허용")
  private Integer sizeY;
}