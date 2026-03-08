package com.komentum.theme.component.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "designComponent 응답 DTO")
public class DesignComponentDto {

  @Schema(description = "designComponent Id", example = "1")
  @JsonProperty("design_component_id")
  private Integer designComponentId;

  @Schema(description = "사용자 공개 ID", example = "1010akak") //예시 확인 필요
  @JsonProperty("public_user_id")
  private String publicUserId;

  @Schema(description = "이미지 URL", example = "https://example.com/image.png")
  @JsonProperty("image_url")
  private String imageUrl;

  @Schema(description = "생성일시", example = "2026-03-08T21:21:08")
  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시", example = "2026-03-08T21:22:18")
  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  @Schema(description = "공개 여부", example = "true")
  @JsonProperty("is_public")
  private Boolean isPublic;
}