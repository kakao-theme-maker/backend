package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
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

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    private String content;

    @JsonProperty("design_component_id")
    private Integer designComponentId;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    private Long prefers;

    public static DesignBoardDetailDto from(Post post, DesignComponent designComponent,
        User author, Long prefers, String previewImageUrl) {
      return DesignBoardDetailDto.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .designComponentId(designComponent.getDesignComponentId())
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .previewImageUrl(previewImageUrl)
          .prefers(prefers)
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DesignBoardPreviewDto {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("design_component_id")
    private Integer designComponentId;

    private String title;

    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    private Long prefers;

    public static DesignBoardPreviewDto from(Post post, DesignComponent designComponent,
        User author, Long prefers, String previewImageUrl) {
      return DesignBoardPreviewDto.builder()
          .postId(post.getPostId())
          .designComponentId(designComponent.getDesignComponentId())
          .title(post.getTitle())
          .previewImageUrl(previewImageUrl)
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

    @JsonProperty("public_flag")
    boolean publicFlag;
  }
}
