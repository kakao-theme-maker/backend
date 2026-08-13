package com.komentum.designcomponent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "designComponent 응답 DTO")
public class DesignComponentDto {

  @Schema(description = "designComponent Id", example = "1")
  private Integer designComponentId;

  @Schema(description = "사용자 공개 ID", example = "1010akak") //예시 확인 필요
  private String publicUserId;

  @Schema(description = "이미지 URL", example = "https://example.com/image.png")
  private String imageUrl;

  @Schema(description = "생성일시", example = "2026-03-08T21:21:08")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시", example = "2026-03-08T21:22:18")
  private LocalDateTime updatedAt;

  @Schema(description = "공개 여부", example = "true")
  private Boolean isPublic;

  @Schema(description = "연결된 component type 목록")
  private List<ComponentTypeDto> componentTypes;
}
