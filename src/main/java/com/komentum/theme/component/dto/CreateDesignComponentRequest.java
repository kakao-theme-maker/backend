package com.komentum.theme.component.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "designComponent 생성 요청 DTO")
public class CreateDesignComponentRequest {

  @Schema(description = "공개 여부", example = "true")
  private Boolean isPublic;
}
