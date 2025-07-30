package com.theme.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PreferDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PreferCreateDto {

    String userEmail;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PreferDeleteDto {

    String userEmail;
  }
}
