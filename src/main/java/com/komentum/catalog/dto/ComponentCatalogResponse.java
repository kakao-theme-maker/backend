package com.komentum.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Schema(description = "테마 + 디자인 에셋 복합 조회 응답 DTO")
public class ComponentCatalogResponse {

  @Schema(description = "데이터 종류 ( THEME | DESIGN )", example = "THEME | DESIGN")
  private ComponentType componentType;
  @Schema(description = "테마 혹은 디자인 에셋의 식별자")
  private Integer componentId;
  @Schema(description = "테마 혹은 디자인 에셋의 대표 이미지")
  private String previewImageUrl;
  @Schema(description = "테마 혹은 디자인 에셋의 생성 시간")
  private String createdAt;

  public static ComponentCatalogResponse of(ComponentSummary summary) {
    return ComponentCatalogResponse.builder()
        .componentType(summary.getType())
        .componentId(summary.getId())
        .previewImageUrl(summary.getPreviewImageUrl())
        .createdAt(summary.getCreatedAt().toString())
        .build();
  }
}
