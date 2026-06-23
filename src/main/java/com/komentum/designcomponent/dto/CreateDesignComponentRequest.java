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
@Schema(description = "designComponent 생성 요청 DTO")
public class CreateDesignComponentRequest {

  @Schema(description = "공개 여부", example = "true")
  private Boolean isPublic;


  // when create, auto match
  @NotNull(message = "componentTypeIds is required")
  @Size(min = 1, message = "componentTypeIds must contain at least one id")
  @Schema(description = "디자인 컴포넌트에 연결할 component type id 목록", example = "[1,2,3]")
  private List<@NotNull(message = "componentTypeIds cannot contain null") Integer> componentTypeIds;
}
