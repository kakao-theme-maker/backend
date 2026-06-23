package com.komentum.designcomponent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
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
@Schema(description = "designComponent 수정 요청 DTO")
public class UpdateDesignComponentRequest {


  @Schema(description = "공개 여부", example = "true")
  private Boolean isPublic;

  @Size(min = 1, message = "componentTypeIds must contain at least one id")
  @Schema(description = "component type id 목록. 전달 시 기존 목록을 전체 교체한다.", example = "[1,2,3]")
  private List<@NotNull(message = "componentTypeIds cannot contain null") Integer> componentTypeIds;
}
