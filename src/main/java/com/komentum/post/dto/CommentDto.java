package com.komentum.post.dto;

import com.komentum.post.domain.Comment;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CommentDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentResponse {

    long commentId;
    String userEmail;
    String content;
    String createdAt;

    public static CommentResponse from(Comment comment) {
      String createdAt = DateTimeFormatter.ISO_DATE_TIME.format(comment.getCreatedAt());
      return CommentResponse.builder()
          .commentId(comment.getCommentId())
          .userEmail(comment.getUser().getUserEmail())
          .content(comment.getContent())
          .createdAt(createdAt)
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentCreateDto {

    String content;
    String userEmail;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CommentUpdateDto {

    String content;
  }
}
