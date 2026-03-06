package com.komentum.theme.component.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDesignComponentRequest {

  private String imageUrl;
  private Boolean isPublic;
}