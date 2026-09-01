package com.komentum.designcomponent.dto;

import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.TypeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "component type 응답용 DTO")
public class ComponentTypeDto {

  @Schema(description = "component type의 식별자")
  private Integer componentTypeId;

  @Schema(description = "component type의 설명")
  private String explain;

  @Schema(description = "component type의 이름")
  private String name;

  // API 응답 시 enum name 대신 typeCode string 사용 ( @JsonValue )
  @Schema(description = "component type의 종류")
  private TypeCode typeCode;

  @Schema(description = "component type이 공통/Android/iOS 중 어디에 속하는지")
  private PlatformScope platformScope;

  @Schema(description = "component type의 생성일", example = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  @Schema(description = "component type의 갱신일", example = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime updatedAt;
}

