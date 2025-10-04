package com.komentum.post.dto;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
      public Long prefers;
      public List<Tag> tags;

      private static PostResponse from(Post post) {
        String createdAtString = DateTimeFormatter.ISO_LOCAL_DATE.format(post.getCreatedAt());
        return PostResponse.builder()
            .postId(post.getPostId())
            .title(post.getTitle())
            .content(post.getContent())
            .createdAt(createdAtString)
            .build();
      }

      public static PostResponse from(PostSummary postSummary, List<Tag> tags) {
        PostResponse postResponse = from(postSummary.getPost());
        postResponse.setTags(tags);
        postResponse.setPrefers(postSummary.getPrefers());
        return postResponse;
      }

      public static PostResponse from(PostDetail postDetail) {
        PostResponse postResponse = from(postDetail.getPost());
        postResponse.setPrefers(postDetail.getPrefers());
        postResponse.setTags(postDetail.getTags());
        return postResponse;
      }
    }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostDetail {

    public Post post;
    public Long prefers;
    public List<Tag> tags;

    public static PostDetail from(PostSummary projection) {
      return PostDetail.builder()
          .post(projection.getPost())
          .prefers(projection.getPrefers())
          .build();
    }

    public static PostDetail from(Post post) {
      return PostDetail.builder()
          .post(post)
          .build();
    }
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
