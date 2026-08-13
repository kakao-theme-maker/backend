package com.komentum.post.dto;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 응답용 component type 요약 DTO")
public class BoardComponentTypeDto {

  @Schema(description = "component type 식별자")
  private Integer componentTypeId;

  @Schema(description = "component type code")
  private TypeCode typeCode;

  @Schema(description = "component type 이름")
  private String name;

  public static BoardComponentTypeDto from(ComponentType componentType) {
    return BoardComponentTypeDto.builder()
        .componentTypeId(componentType.getComponentTypeId())
        .typeCode(componentType.getTypeCode())
        .name(componentType.getName())
        .build();
  }
}
