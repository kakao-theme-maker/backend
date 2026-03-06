package com.komentum.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.komentum.global.utils.DateUtils;
import com.komentum.post.domain.Post;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "게시글 생성일", example = "YYYY-mm-dd")
    @JsonProperty("created_at")
    private String createdAt;

    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    @JsonProperty("preview_image_url")
    private String previewImageUrl;

    @Schema(description = "게시글 좋아요 수")
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
  @Schema(description = "디자인 에셋 게시글 생성 요청 DTO")
  public static class DesignBoardCreateDto {

    @Schema(description = "디자인 에셋 게시글 제목")
    String title;

    @Schema(description = "디자인 에셋 게시글 내용")
    String content;

    @Schema(description = "디자인 에셋 게시글에 대한 디자인 에셋 데이터 ID")
    int designComponentId;

    @Schema(description = "공개여부")
    @JsonProperty("public_flag")
    boolean publicFlag;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "디자인 에셋 게시글 수정 요청 DTO")
  public static class DesignBoardUpdateDto {

    @Schema(description = "수정할 디자인 에셋 게시글 제목(null 가능)")
    String title;

    @Schema(description = "수정할 디자인 에셋 게시글 내용(null 가능)")
    String content;

    @Schema(description = "공개여부(null 허용)")
    @JsonProperty("public_flag")
    boolean publicFlag;
  }
}
