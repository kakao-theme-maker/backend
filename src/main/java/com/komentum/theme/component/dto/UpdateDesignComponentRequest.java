package com.komentum.theme.component.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "designComponent 수정 요청 DTO")
public class UpdateDesignComponentRequest {


  @Schema(description = "공개 여부", example = "true")
  private Boolean isPublic;
}