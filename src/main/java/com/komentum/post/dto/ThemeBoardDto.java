package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: 태그 기능 필요 시 추후 태그 부분 주석 제거하기
public class ThemeBoardDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardDetailDto {

    @JsonProperty("post_id")
    private Long postId;

    private String title;

    private String content;

    @JsonProperty("theme_component_id")
    private Integer themeComponentId;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    private Long prefers;

//    private List<TagResponse> tags;

    public static ThemeBoardDetailDto from(Post post, ThemeComponent themeComponent, User author,
//        List<Tag> tags,
        Long prefers, String previewImageUrl) {
      return ThemeBoardDetailDto.builder()
          .postId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .themeComponentId(themeComponent.getThemeComponentId())
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .previewImageUrl(previewImageUrl)
          .prefers(prefers)
//          .tags(tags.stream().map(TagResponse::from).toList())
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardPreviewDto {

    @JsonProperty("post_id")
    private Long postId;

    @JsonProperty("theme_component_id")
    private Integer themeComponentId;

    private String title;

    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    private Long prefers;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardCreateDto {

    String title;

    String content;

//    @JsonProperty("post_tags")
//    List<TagCreateDto> postTags;

    int themeComponentId;

    @JsonProperty("public_flag")
    boolean publicFlag;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardUpdateDto {

    String title;

    String content;

//    @JsonProperty("post_tags")
//    List<TagUpdateDto> postTags;

    @JsonProperty("public_flag")
    boolean publicFlag;
  }
}
