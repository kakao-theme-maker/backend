package com.komentum.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ComponentCatalogResponse {

  private ComponentType componentType;
  private Integer componentId;
  private String previewImageUrl;
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
