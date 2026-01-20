package com.komentum.post.dto;

import com.komentum.post.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CategoryDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryResponseDto {

    long categoryId;
    String name;

    public static CategoryResponseDto from(Category category) {
      return CategoryResponseDto.builder()
          .categoryId(category.getCategoryId())
          .name(category.getName()).build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryCreateDto {

    String name;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryUpdateDto {

    String name;
  }
}
