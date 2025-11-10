package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.theme.theme.domain.ThemeComponent;
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

    private Long boardId;
    private String title;
    private String content;
    private Long themeComponentId;
    private String userEmail;
    private String createdAt;
    private String profileImageUrl;
    private Long prefers;
    private List<TagResponse> tags;


    public static ThemeBoardDetailDto fromNewBoard(Post post) {
      return ThemeBoardDetailDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .content(post.getContent())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .build();
    }

    public static ThemeBoardDetailDto from(PostSummary postSummary, List<Tag> tags) {
      ThemeBoardDetailDto themeBoardDetailDto = fromNewBoard(postSummary.getPost());
      themeBoardDetailDto.setTags(
          tags.stream().map(TagResponse::from).toList());
      themeBoardDetailDto.setPrefers(postSummary.getPrefers());
      return themeBoardDetailDto;
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ThemeBoardPreviewDto {

    private Long boardId;
    private Integer themeComponentId;
    private String title;
    private String profileImageUrl;
    private String userEmail;
    private String createdAt;
    private Long prefers;

    public static ThemeBoardPreviewDto from(Post post, ThemeComponent themeComponent,
        Long prefers) {
      return ThemeBoardPreviewDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .userEmail(post.getUser().getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
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

    @JsonProperty("profile_image_url")
    String profileImageUrl;

    @JsonProperty("post_tags")
    List<TagCreateDto> postTags;

    int themeComponentId;

    @JsonProperty("is_public")
    boolean isPublic;

    public PostCreateDto toPostCreateDto() {
      return PostCreateDto.builder()
          .title(title)
          .content(content)
          .profileImageUrl(profileImageUrl)
          .isPublic(isPublic)
          .build();
    }
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

    @JsonProperty("is_public")
    boolean isPublic;
  }
}
