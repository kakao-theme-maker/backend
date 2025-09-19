package com.komentum.theme.component.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDesignComponentRequest {

  @NotBlank(message = "사용자 이메일은 필수입니다")
  private String userEmail;

  private Integer componentTypeId; // Component Type ID

  private String imageUrl;

  private Boolean isPublic;
}
