package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ThemeBoardDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardDetailDto {

    @JsonProperty("board_id")
    private Long boardId;

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

    private List<TagResponse> tags;

    public static ThemeBoardDetailDto from(Post post, ThemeComponent themeComponent, User author,
        List<Tag> tags,
        Long prefers, String previewImageUrl) {
      return ThemeBoardDetailDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .themeComponentId(themeComponent.getThemeComponentId())
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .previewImageUrl(previewImageUrl)
          .prefers(prefers)
          .tags(tags.stream().map(TagResponse::from).toList())
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardPreviewDto {

    @JsonProperty("board_id")
    private Long boardId;

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

    public static ThemeBoardPreviewDto from(Post post, ThemeComponent themeComponent, User author,
        Long prefers, String previewImageUrl) {
      return ThemeBoardPreviewDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .previewImageUrl(previewImageUrl)
          .themeComponentId(themeComponent.getThemeComponentId())
          .prefers(prefers)
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardCreateDto {

    String title;

    String content;

    @JsonProperty("user_email")
    String userEmail;

    @JsonProperty("post_tags")
    List<TagCreateDto> postTags;

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

    @JsonProperty("user_email")
    String userEmail;

    @JsonProperty("post_tags")
    List<TagUpdateDto> postTags;

    @JsonProperty("public_flag")
    boolean publicFlag;
  }
}
