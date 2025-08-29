package com.komentum.post.dto;

import com.komentum.post.domain.Post;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostResponse {

    public Long postId;
    public String title;
    public String content;
    public String createdAt;
    public Long prefer;

    public static PostResponse from(Post post) {
      String createdAtString = DateTimeFormatter.ISO_LOCAL_DATE.format(post.getCreatedAt());
      return PostResponse.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .createdAt(createdAtString)
          .build();
    }

    public static PostResponse from(PostRawData postRawData) {
      PostResponse postResponse = from(postRawData.getPost());
      postResponse.setPrefer(postRawData.getPrefer());
      return postResponse;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostRawData {

    public Post post;
    public Long prefer;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostCreateDto {

    public String userEmail;
    public String title;
    public String content;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostUpdateDto {

    public String title;
    public String content;
  }
}
