package com.komentum.designcomponent.dto;

import com.komentum.designcomponent.enums.TypeCode;
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
@Schema(description = "component type 갱신 요청용 DTO")
public class ComponentTypeUpdateRequest {

  @Schema(description = "component type 설명, null 허용")
  private String explain;

  @NotBlank(message = "이름은 필수입니다")
  @Schema(description = "수정할 component type의 이름, not null")
  private String name;

  @NotNull(message = "타입 정보는 필수입니다")
  @Schema(description = "수정할 component type의 종류, not null")
  private TypeCode typeCode;
}