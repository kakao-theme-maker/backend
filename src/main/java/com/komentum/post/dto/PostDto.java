package com.komentum.post.dto;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.facade.BoardManagementHelper;
import com.komentum.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostDto {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PostCreateDto {

    String title;
    String content;
    boolean publicFlag;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostUpdateDto {

    String title;
    String content;
    String previewImageName;
    boolean publicFlag;
  }

  // 사용자가 작성 / 업로드한 게시글 목록 조회
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "사용자의 게시글 목록 조회 응답 DTO")
  public static class UserPostListResponseDto {

    @Schema(description = "게시글 ID")
    Long postId;
    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    String previewImageUrl;
    @Schema(description = "게시글 생성일")
    LocalDateTime createdAt;
    @Schema(description = "게시글 갱신일")
    LocalDateTime updatedAt;
    @Schema(description = "게시글 종류 ( THEME_BOARD | DESIGN_BOARD )", example = "THEME_BOARD | DESIGN_BOARD")
    PostType postType;
    @Schema(description = "게시글 작성자 이름")
    String authorName;
    @Schema(description = "게시글 작성자 프로필 이미지 URL")
    String authorProfileImageUrl;

    public static UserPostListResponseDto from(Post post,
        BoardManagementHelper boardManagementHelper) {
      User author = post.getUser();
      return UserPostListResponseDto.builder()
          .postId(post.getPostId())
          .previewImageUrl(boardManagementHelper.findPreviewImageUrl(post.getPreviewImageName()))
          .createdAt(post.getCreatedAt())
          .updatedAt(post.getUpdatedAt())
          .postType(post.getPostType())
          .authorName(author.getName())
          .authorProfileImageUrl(author.getProfileImg())
          .build();
    }
  }

}
