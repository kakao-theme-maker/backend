package com.komentum.theme.component.dto;

import com.komentum.theme.component.domain.DesignComponent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignComponentResponse {

  private Integer designComponentId;
  private String userEmail;
  private String imageUrl;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Boolean isPublic;

  public static DesignComponentResponse from(DesignComponent designComponent) {
    return DesignComponentResponse.builder()
        .designComponentId(designComponent.getDesignComponentId())
        .userEmail(designComponent.getUserEmail())
        .imageUrl(designComponent.getImageUrl())
        .createdAt(designComponent.getCreatedAt())
        .updatedAt(designComponent.getUpdatedAt())
        .isPublic(designComponent.getIsPublic())
        .build();
  }
}