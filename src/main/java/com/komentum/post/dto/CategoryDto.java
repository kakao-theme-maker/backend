package com.komentum.post.dto;

import com.komentum.post.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CategoryDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "카테고리 정보 응답 DTO")
  public static class CategoryResponseDto {

    @Schema(description = "카테고리 식별자(ID)", example = "1")
    long categoryId;
    @Schema(description = "카테고리 이름", example = "테스트 카테고리")
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
  @Schema(description = "카테고리 생성 요청 DTO")
  public static class CategoryCreateDto {

    @Schema(description = "생성할 카테고리 이름", example = "새로운 카테고리")
    String name;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "카테고리 수정 요청 DTO")
  public static class CategoryUpdateDto {

    @Schema(description = "수정할 카테고리 이름", example = "수정된 카테고리")
    String name;
  }
}
