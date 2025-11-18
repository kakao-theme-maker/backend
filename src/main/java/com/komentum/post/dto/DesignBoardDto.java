package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class DesignBoardDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DesignBoardDetailDto {

    @JsonProperty("board_id")
    private Long boardId;

    private String title;

    private String content;

    @JsonProperty("design_component_id")
    private Integer designComponentId;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    private Long prefers;

    private List<TagResponse> tags;

    public static DesignBoardDetailDto from(Post post, DesignComponent designComponent,
        User author, List<Tag> tags, Long prefers, String profileImageUrl) {
      return DesignBoardDetailDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .designComponentId(designComponent.getDesignComponentId())
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .profileImageUrl(profileImageUrl)
          .prefers(prefers)
          .tags(tags.stream().map(TagResponse::from).toList())
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DesignBoardPreviewDto {

    @JsonProperty("board_id")
    private Long boardId;

    @JsonProperty("design_component_id")
    private Integer designComponentId;

    private String title;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    private Long prefers;

    public static DesignBoardPreviewDto from(Post post, DesignComponent designComponent,
        User author, Long prefers, String profileImageUrl) {
      return DesignBoardPreviewDto.builder()
          .boardId(post.getPostId())
          .designComponentId(designComponent.getDesignComponentId())
          .title(post.getTitle())
          .profileImageUrl(profileImageUrl)
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .prefers(prefers)
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DesignBoardCreateDto {

    String title;

    String content;

    @JsonProperty("user_email")
    String userEmail;

    @JsonProperty("profile_image")
    String profileImage;

    @JsonProperty("post_tags")
    List<TagCreateDto> postTags;

    int designComponentId;

    @JsonProperty("public_flag")
    boolean publicFlag;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DesignBoardUpdateDto {

    String title;

    String content;

    @JsonProperty("user_email")
    String userEmail;

    @JsonProperty("post_tags")
    List<TagUpdateDto> postTags;

    @JsonProperty("public_flag")
    boolean publicFlag;
  }
}
