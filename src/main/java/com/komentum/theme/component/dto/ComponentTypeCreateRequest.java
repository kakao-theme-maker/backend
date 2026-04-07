package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "component type 생성을 위한 요청 DTO")
public class ComponentTypeCreateRequest {

  @NotBlank(message = "설명은 필수입니다")
  @Schema(description = "생성할 component type의 설명이고, null 불가")
  private String explain;

  @NotNull(message = "플랫폼은 필수입니다")
  @Schema(description = "생성할 component type의 플랫폼이고, null 불가", example = "ANDROID | IOS")
  private Platform platform;

  @NotBlank(message = "컴포넌트 경로는 필수입니다")
  @Schema(description = "생성할 component type의 경로이고, null 불가", example = "/path/to/component-type")
  private String componentPath;

  @NotBlank(message = "컴포넌트 이름은 필수입니다")
  @Schema(description = "생성할 component type의 이름이고, null 불가", example = "컴포넌트의 이름")
  private String componentName;

  @Schema(description = "생성할 component type에 해당하는 이미지의 X축 크기")
  private Integer sizeX;

  @Schema(description = "생성할 component type에 해당하는 이미지의 Y축 크기")
  private Integer sizeY;
}