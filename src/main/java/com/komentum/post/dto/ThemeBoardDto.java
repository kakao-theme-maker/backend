package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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
  @Schema(description = "테마 게시글 상세정보 응답 DTO")
  public static class ThemeBoardDetailDto {

    @Schema(description = "게시글 ID")
    @JsonProperty("post_id")
    private Long postId;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "테마 데이터 ID")
    @JsonProperty("theme_component_id")
    private Integer themeComponentId;

    @Schema(description = "작성자 이메일")
    @JsonProperty("user_email")
    private String userEmail;

    @Schema(description = "작성자 이름")
    @JsonProperty("user_name")
    private String userName;

    @Schema(description = "게시글 생성일")
    @JsonProperty("created_at")
    private String createdAt;

    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    @Schema(description = "게시글 좋아요 수")
    private Long prefers;

    @Schema(description = "댓글 수")
    private Long comments;

    @Schema(description = "태그 목록")
    private List<TagResponse> tags;

    @Schema(description = "현재 사용자의 좋아요 여부")
    private boolean liked;

    @Schema(description = "현재 사용자의 북마크 저장 여부")
    private boolean bookmarked;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "테마 게시글 간략정보 응답 DTO")
  public static class ThemeBoardPreviewDto {

    @Schema(description = "게시글 ID")
    @JsonProperty("post_id")
    private Long postId;

    @Schema(description = "테마 데이터 ID")
    @JsonProperty("theme_component_id")
    private Integer themeComponentId;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    @Schema(description = "게시글 작성자 이메일")
    @JsonProperty("user_email")
    private String userEmail;

    @Schema(description = "게시글 생성일")
    @JsonProperty("created_at")
    private String createdAt;

    @Schema(description = "게시글 좋아요 수")
    private Long prefers;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(
      description = """
          테마 게시글 작성 요청 DTO
          
          - title*(String): 게시글 제목
          - content*(String): 게시글 내용
          - themeComponentId*(Integer): 게시글을 작성할 테마 ID
          - publicFlag(Boolean): 게시글 공개 여부
          """
  )
  public static class ThemeBoardCreateDto {

    @Schema(description = "게시글 ID")
    String title;

    @Schema(description = "게시글 내용")
    String content;

//    @JsonProperty("post_tags")
//    List<TagCreateDto> postTags;

    @Schema(description = "테마 데이터 ID")
    int themeComponentId;

    @Schema(description = "게시글 공개여부")
    @JsonProperty("public_flag")
    boolean publicFlag;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(
      description = """
          테마 게시글 수정 요청 DTO
          
          - title(String): 게시글 제목 ( null 허용 )
          - content(String): 게시글 내용 ( null 허용 )
          - publicFlag(Boolean): 게시글 공개 여부 ( null 허용 )
          """
  )
  public static class ThemeBoardUpdateDto {

    @Schema(description = "게시글 제목")
    String title;

    @Schema(description = "게시글 내용")
    String content;

//    @JsonProperty("post_tags")
//    List<TagUpdateDto> postTags;

    @Schema(description = "게시글 공개여부")
    @JsonProperty("public_flag")
    boolean publicFlag;
  }
}
