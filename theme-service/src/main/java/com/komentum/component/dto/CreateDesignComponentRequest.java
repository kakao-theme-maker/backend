package com.komentum.component.dto;

import lombok.Data;

@Data
public class CreateDesignComponentRequest {

  private String userEmail;

  private Integer componentTypeId; // Component Type ID

  private String imageUrl;

  private Boolean isPublic;
}
