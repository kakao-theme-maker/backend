package com.komentum.post.dto;

import com.komentum.post.domain.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "댓글 공통 응답 DTO")
  public static class CommentResponse {

    @Schema(description = "댓글 ID")
    Long commentId;
    @Schema(description = "댓글 작성자 이메일")
    String userEmail;
    @Schema(description = "댓글 내용")
    String content;
    @Schema(description = "댓글 생성일")
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
  @Schema(description = "댓글 생성 요청 DTO")
  public static class CommentCreateDto {

    @Schema(description = "생성할 댓글 내용")
    String content;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "댓글 갱신 요청 DTO")
  public static class CommentUpdateDto {

    @Schema(description = "수정할 댓글 내용")
    String content;
  }
}
