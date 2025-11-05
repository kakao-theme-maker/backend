package com.komentum.post.dto;

import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PostDto {

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
    private List<Tag> tags;


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
      themeBoardDetailDto.setTags(tags);
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
    private String title;
    private String profileImageUrl;
    private String userEmail;
    private String createdAt;
    private Long prefers;

    public static ThemeBoardPreviewDto from(Post post, Long prefers) {
      return ThemeBoardPreviewDto.builder()
          .boardId(post.getPostId())
          .title(post.getTitle())
          .userEmail(post.getUser().getUserEmail())
          .createdAt(DateUtils.convertToDateString(post.getCreatedAt()))
          .prefers(prefers)
          .build();
    }
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostCreateDto {

    private String title;
    private String content;
    private String userEmail;
    private List<TagCreateDto> tags;
    private boolean isPublic;
    private long themeComponentId;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PostUpdateDto {

    String title;
    String content;
    String userEmail;
    List<TagUpdateDto> postTags;
    boolean isPublic;
  }
}
