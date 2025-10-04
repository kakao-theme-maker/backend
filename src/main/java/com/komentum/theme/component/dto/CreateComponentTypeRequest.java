package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
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
public class CreateComponentTypeRequest {

  @NotBlank(message = "설명은 필수입니다")
  private String explain;

  @NotNull(message = "플랫폼은 필수입니다")
  private Platform platform;

  @NotBlank(message = "컴포넌트 경로는 필수입니다")
  private String componentPath;

  @NotBlank(message = "컴포넌트 이름은 필수입니다")
  private String componentName;

  private Integer sizeX;

  private Integer sizeY;
}