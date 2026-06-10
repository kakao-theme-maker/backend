package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Tag;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.query.PostQuery;
import com.komentum.post.facade.BoardManagementHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  // 사용자가 작성 / 업로드 / 북마크 / 좋아요한 게시글 목록 조회 응답
  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "사용자의 게시글 목록 조회 응답 DTO")
  public static class UserPostListResponseDto {

    @Schema(description = "게시글 ID")
    @JsonProperty("post_id")
    Long postId;

    @Schema(description = "게시글 종류 ( THEME_BOARD | DESIGN_BOARD )")
    @JsonProperty("post_type")
    PostType postType;

    @Schema(description = "테마/디자인 컴포넌트 ID (postType 에 따라 themeComponentId 또는 designComponentId)")
    @JsonProperty("component_id")
    Integer componentId;

    @Schema(description = "게시글 제목")
    String title;

    @Schema(description = "게시글 내용")
    String content;

    @Schema(description = "태그 목록")
    List<TagResponse> tags;

    @Schema(description = "게시글 대표 이미지 URL 목록", example = "[https://sample.com, ... ]")
    @JsonProperty("preview_image_url")
    List<String> previewImageUrl;

    @Schema(description = "게시글 생성일")
    @JsonProperty("created_at")
    String createdAt;

    @Schema(description = "게시글 갱신일")
    @JsonProperty("updated_at")
    String updatedAt;

    @Schema(description = "게시글 작성자 이름")
    @JsonProperty("user_name")
    String userName;

    @Schema(description = "게시글 작성자 프로필 이미지 URL")
    @JsonProperty("profile_image")
    String profileImage;

    @Schema(description = "게시글 좋아요 수")
    Long prefers;

    @Schema(description = "게시글 댓글 수")
    Long comments;

    @Schema(description = "현재 사용자의 좋아요 여부")
    boolean liked;

    @Schema(description = "현재 사용자의 북마크 여부")
    boolean bookmarked;

    public static UserPostListResponseDto from(PostQuery.UserPostListRow row, List<Tag> tags,
        BoardManagementHelper boardManagementHelper) {
      List<TagResponse> tagResponses = tags.stream().map(TagResponse::from).toList();
      String previewImageUrl = boardManagementHelper.findPreviewImageUrl(
          row.getPreviewImageName());
      List<String> previewImageUrls =
          previewImageUrl == null ? List.of() : List.of(previewImageUrl);
      return UserPostListResponseDto.builder()
          .postId(row.getPostId())
          .postType(row.getPostType())
          .componentId(row.getComponentId())
          .title(row.getTitle())
          .content(row.getContent())
          .tags(tagResponses)
          .previewImageUrl(previewImageUrls)
          .createdAt(DateUtils.convertToDateString(row.getCreatedAt()))
          .updatedAt(DateUtils.convertToDateString(row.getUpdatedAt()))
          .userName(row.getUserName())
          .profileImage(row.getProfileImage())
          .prefers(row.getPrefers())
          .comments(row.getComments())
          .liked(row.isLiked())
          .bookmarked(row.isBookmarked())
          .build();
    }
  }

}
