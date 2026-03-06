package com.komentum.theme.component.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDesignComponentRequest {

  private String imageUrl;

  private Boolean isPublic;
}
