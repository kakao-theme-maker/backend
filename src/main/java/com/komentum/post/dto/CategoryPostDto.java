package com.komentum.post.dto;

import com.komentum.post.domain.CategoryPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CategoryPostDto {

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "카테고리 게시글 매핑 응답 DTO")
  public static class CategoryPostResponse {

    @Schema(description = "카테고리 ID")
    Long categoryId;
    @Schema(description = "게시글 ID")
    Long postId;

    public static CategoryPostResponse from(CategoryPost categoryPost) {
      return CategoryPostResponse.builder()
          .categoryId(categoryPost.getCategory().getCategoryId())
          .postId(categoryPost.getPost().getPostId())
          .build();
    }
  }
}
