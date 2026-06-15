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

public class DesignBoardDto {

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "디자인 에셋 게시글 상세 정보 응답 DTO")
  public static class DesignBoardDetailDto {

    @Schema(description = "게시글 ID")
    @JsonProperty("post_id")
    private Long postId;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "디자인 에셋 데이터 ID")
    @JsonProperty("design_component_id")
    private Integer designComponentId;

    @Schema(description = "게시글 작성자 이메일")
    @JsonProperty("user_email")
    private String userEmail;

    @Schema(description = "게시글 작성자 이름")
    @JsonProperty("user_name")
    private String userName;

    @Schema(description = "게시글 생성일", example = "YYYY-mm-dd")
    @JsonProperty("created_at")
    private String createdAt;

    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    @JsonProperty("preview_image_url")
    private List<String> previewImageUrl;

    @Schema(description = "게시글 좋아요 수")
    private Long prefers;

    @Schema(description = "댓글 수")
    private Long comments;

    @Schema(description = "태그 목록")
    private List<TagResponse> tags;

    @Schema(description = "현재 사용자 좋아요 여부")
    private boolean liked;

    @Schema(description = "현재 사용자의 북마크 저장 여부")
    private boolean bookmarked;

    @Schema(description = "게시글 작성자 프로필 이미지 URL")
    @JsonProperty("profile_image")
    private String profileImage;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "디자인 에셋 게시글 간략정보 응답 DTO")
  public static class DesignBoardPreviewDto {

    @Schema(description = "게시글 ID")
    @JsonProperty("post_id")
    private Long postId;

    @Schema(description = "디자인 에셋 데이터 ID")
    @JsonProperty("design_component_id")
    private Integer designComponentId;

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

    @Schema(description = "좋아요 수")
    private Long prefers;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(
      description = """
          디자인 에셋 게시글 생성 요청 DTO
          
          - title (String, not null) : 새로운 게시글 제목
          - content (String, not null) : 새로운 게시글 내용
          - designComponentIds(List<String>, not null) : 게시글을 생성할 design component ID 목록
          - publicFlag (String, optional) : 게시글 공개 여부 (null 허용)
          
          - post_tags (List<TagCreateDto>, optional) : 생성할 게시글 태그 목록 (전체 덮어쓰기)
            - TagCreateDto 구조:
              - tag_name (String, not null) : 생성할 태그 이름
          """
  )
  public static class DesignBoardCreateDto {

    @Schema(description = "디자인 에셋 게시글 제목")
    String title;

    @Schema(description = "디자인 에셋 게시글 내용")
    String content;

    @Schema(description = "디자인 에셋 게시글에 대한 디자인 에셋 데이터 ID")
    List<Integer> designComponentIds;

    @Schema(description = "공개여부")
    @JsonProperty("public_flag")
    boolean publicFlag;

    @Schema(description = "태그 목록")
    @JsonProperty("post_tags")
    List<TagCreateDto> postTags;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(
      description = """
          디자인 에셋 게시글 수정 요청 DTO
          
          - title (String, optional) : 새로운 게시글 제목 (null 허용)
          - content (String, optional) : 새로운 게시글 내용 (null 허용)
          - publicFlag (String, optional) : 게시글 공개 여부 (null 허용)
          - designComponentIds (String, optional) : 현재 디자인 에셋 목록으로 디자인 에셋 게시글을 덮어쓴다
          
          - post_tags (List<TagUpdateDto>, optional) : 게시글 태그 목록 (전체 덮어쓰기)
            - TagUpdateDto 구조:
              - tag_name (String, not null) : 생성할 태그 이름
          """
  )
  public static class DesignBoardUpdateDto {

    @Schema(description = "수정할 디자인 에셋 게시글 제목(null 가능)", example = "수정할 디자인 에셋 게시글 제목(null 가능)")
    String title;

    @Schema(description = "수정할 디자인 에셋 게시글 내용(null 가능)", example = "수정할 디자인 에셋 게시글 내용(null 가능)")
    String content;

    @Schema(description = "현재 디자인 에셋 목록으로 디자인 에셋 게시글을 덮어쓴다(null 허용)")
    List<Integer> designComponentIds;

    @Schema(description = "공개여부(null 허용)", example = "공개여부(null 허용) : true|false")
    @JsonProperty("public_flag")
    boolean publicFlag;

    @Schema(description = "게시글에 덮어쓸 태그 목록 ( 이 목록으로 태그 목록이 변경됨 )")
    @JsonProperty("post_tags")
    List<TagUpdateDto> postTags;
  }
}
