package com.komentum.post.dto;

import com.komentum.post.domain.Post;
import com.komentum.post.facade.BoardManagementHelper;
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
    boolean publicFlag;
  }

  // 사용자가 작성 / 업로드한 게시글 목록 조회
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserPostListResponseDto {

    Long postId;
    String previewImageUrl;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static UserPostListResponseDto from(Post post,
        BoardManagementHelper boardManagementHelper) {
      return UserPostListResponseDto.builder()
          .postId(post.getPostId())
          .previewImageUrl(boardManagementHelper.findPreviewImageUrl(post.getPostId()))
          .createdAt(post.getCreatedAt())
          .updatedAt(post.getUpdatedAt())
          .build();
    }
  }

}
