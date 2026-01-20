package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.post.domain.CategoryPost;
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
  public static class CategoryPostResponse {

    @JsonProperty("category_id")
    Long categoryId;
    @JsonProperty("post_id")
    Long postId;

    public static CategoryPostResponse from(CategoryPost categoryPost) {
      return CategoryPostResponse.builder()
          .categoryId(categoryPost.getCategory().getCategoryId())
          .postId(categoryPost.getPost().getPostId())
          .build();
    }
  }
}
