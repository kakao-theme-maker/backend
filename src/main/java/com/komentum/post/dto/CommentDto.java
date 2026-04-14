package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @Schema(description = "댓글 작성자 이름")
    String userName;
    @Schema(description = "댓글 작성자 프로필 이미지 URL")
    String profileImageUrl;
    @Schema(description = "댓글 내용")
    String content;
    @Schema(description = "댓글 좋아요 수")
    Long likeCount;
    @Schema(description = "현재 로그인 사용자의 댓글 좋아요 여부")
    @JsonProperty("isLiked")
    Boolean isLiked;
    @Schema(description = "댓글 생성일")
    String createdAt;

    public static CommentResponse from(Comment comment) {
      return from(comment, false);
    }

    public static CommentResponse from(Comment comment, boolean liked) {
      String createdAt = DateTimeFormatter.ISO_DATE_TIME.format(comment.getCreatedAt());
      return CommentResponse.builder()
          .commentId(comment.getCommentId())
          .userEmail(comment.getUser().getUserEmail())
          .userName(comment.getUser().getName())
          .profileImageUrl(comment.getUser().getProfileImg())
          .content(comment.getContent())
          .likeCount(comment.getLikeCount())
          .isLiked(liked)
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
