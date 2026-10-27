package com.komentum.post.dto;

import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
  @Schema(description = "게시글에 연결할 디자인 에셋과 타입 ID 목록")
  public static class DesignComponentMappingDto {

    @NotNull
    private Integer designComponentId;

    @NotEmpty
    private List<@NotNull Integer> componentTypeIds;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "디자인 에셋 게시글 상세 정보 응답 DTO")
  public static class DesignBoardDetailDto {

    @Schema(description = "게시글 ID")
    private Long postId;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "디자인 에셋 데이터 ID")
    private Integer designComponentId;

    @Schema(description = "게시글 작성자 이메일")
    private String userEmail;

    @Schema(description = "게시글 작성자 이름")
    private String userName;

    @Schema(description = "게시글 생성일", example = "YYYY-mm-dd")
    private String createdAt;

    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    private List<String> previewImageUrl;

    @Schema(description = "게시글 좋아요 수")
    private Long prefers;

    @Schema(description = "댓글 수")
    private Long comments;

    @Schema(description = "태그 목록")
    private List<TagResponse> tags;

    @Schema(description = "현재 사용자 좋아요 여부 (비인증 사용자는 false)")
    private boolean liked;

    @Schema(description = "현재 사용자의 북마크 저장 여부 (비인증 사용자는 false)")
    private boolean bookmarked;

    @Schema(description = "현재 사용자의 게시글 작성자 팔로우 여부 (비인증 사용자는 false)")
    private boolean following;

    @Schema(description = "게시글 작성자 프로필 이미지 URL")
    private String profileImage;

    @Schema(description = "게시글에 연결된 component type 목록")
    private List<BoardComponentTypeDto> componentTypes;

    @Schema(description = "디자인 에셋별 게시글 타입 매핑")
    private List<DesignComponentMappingDto> designComponents;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "디자인 에셋 게시글 간략정보 응답 DTO")
  public static class DesignBoardPreviewDto {

    @Schema(description = "게시글 ID")
    private Long postId;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 대표 이미지 URL", example = "https://sample.com")
    private String previewImageUrl;

    @Schema(description = "게시글 작성자 이메일")
    private String userEmail;

    @Schema(description = "게시글 생성일")
    private String createdAt;

    @Schema(description = "좋아요 수")
    private Long prefers;

    @Schema(description = "게시글에 연결된 component type 목록")
    private List<BoardComponentTypeDto> componentTypes;
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
          - designComponents (List<DesignComponentMappingDto>, not empty) : 디자인 에셋별 타입 매핑
          - publicFlag (boolean, optional) : 게시글 공개 여부 (미전달 시 false)
          
          - postTags (List<TagCreateDto>, optional) : 생성할 게시글 태그 목록 (전체 덮어쓰기)
            - TagCreateDto 구조:
              - tagName (String, not null) : 생성할 태그 이름
          """
  )
  public static class DesignBoardCreateDto {

    @Schema(description = "디자인 에셋 게시글 제목")
    @Size(max = 100, message = "title must be at most 100 characters")
    String title;

    @Schema(description = "디자인 에셋 게시글 내용")
    @Size(max = 2000, message = "content must be at most 2000 characters")
    String content;

    @Schema(description = "게시글에 연결할 디자인 에셋별 타입 매핑")
    @Valid
    @NotEmpty
    List<@NotNull DesignComponentMappingDto> designComponents;

    @Schema(description = "공개 여부(미전달 시 false)", example = "true")
    boolean publicFlag;

    @Schema(description = "태그 목록")
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
          - publicFlag (boolean, optional) : 게시글 공개 여부 (미전달 시 false)
          - designComponents (List<DesignComponentMappingDto>, optional) : 에셋별 타입 매핑 전체 변경
          
          - postTags (List<TagUpdateDto>, optional) : 게시글 태그 목록 (전체 덮어쓰기)
            - TagUpdateDto 구조:
              - tagName (String, not null) : 생성할 태그 이름
          """
  )
  public static class DesignBoardUpdateDto {

    @Schema(description = "수정할 디자인 에셋 게시글 제목(null 가능)", example = "수정할 디자인 에셋 게시글 제목(null 가능)")
    @Size(max = 100, message = "title must be at most 100 characters")
    String title;

    @Schema(description = "수정할 디자인 에셋 게시글 내용(null 가능)", example = "수정할 디자인 에셋 게시글 내용(null 가능)")
    @Size(max = 2000, message = "content must be at most 2000 characters")
    String content;

    @Schema(description = "에셋별 타입 매핑 전체 변경(null이면 유지, 빈 목록 불가)")
    @Valid
    @Size(min = 1)
    List<@NotNull DesignComponentMappingDto> designComponents;

    @Schema(description = "공개 여부(미전달 시 false)", example = "true")
    boolean publicFlag;

    @Schema(description = "게시글에 덮어쓸 태그 목록 ( 이 목록으로 태그 목록이 변경됨 )")
    List<TagUpdateDto> postTags;
  }
}
