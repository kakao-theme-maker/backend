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

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    private Long prefers;

    private List<TagResponse> tags;

    public static ThemeBoardDetailDto fromNewBoard(Post post, String profileImageUrl) {
      return ThemeBoardDetailDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .profileImageUrl(profileImageUrl)
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .build();
    }

    public static ThemeBoardDetailDto from(Post post, ThemeComponent themeComponent, User author,
        List<Tag> tags,
        Long prefers, String profileImageUrl) {
      ThemeBoardDetailDto themeBoardDetailDto = fromNewBoard(post, profileImageUrl);
      themeBoardDetailDto.setTags(
          tags.stream().map(TagResponse::from).toList());
      themeBoardDetailDto.setPrefers(prefers);
      themeBoardDetailDto.setUserEmail(author.getUserEmail());
      themeBoardDetailDto.setThemeComponentId(themeComponent.getThemeComponentId());
      return themeBoardDetailDto;
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

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("created_at")
    private String createdAt;

    private Long prefers;

    public static ThemeBoardPreviewDto from(Post post, ThemeComponent themeComponent, User author,
        Long prefers, String profileImageUrl) {
      return ThemeBoardPreviewDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .userEmail(author.getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .profileImageUrl(profileImageUrl)
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

    @JsonProperty("profile_image")
    String profileImage;

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
