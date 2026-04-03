package com.komentum.theme.component.dto;

import com.komentum.theme.component.enums.Platform;
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

  @Schema(description = "component type의 플랫폼", example = "ANDROID | IOS")
  private Platform platform;

  @Schema(description = "component type의 경로")
  private String componentPath;

  @Schema(description = "component type의 이름")
  private String componentName;

  @Schema(description = "component type의 x축 크기")
  private Integer sizeX;

  @Schema(description = "component type의 y축 크기")
  private Integer sizeY;

  @Schema(description = "component type의 생성일", example = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;
  
  @Schema(description = "component type의 갱신일", example = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime updatedAt;
}

