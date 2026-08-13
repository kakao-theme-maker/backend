package com.komentum.post.controller;

import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.facade.DesignBoardManagementFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/design-boards")
@RequiredArgsConstructor
public class DesignBoardController {

  private final DesignBoardManagementFacade designBoardManagementFacade;

  /**
   * 디자인 에셋 게시글 목록 조회
   */
  @GetMapping
  @Operation(summary = "현재 인증된 사용자가 디자인 에셋 게시글 목록을 조회힌다")
  public ResponseEntity<List<DesignBoardPreviewDto>> findDesignBoards(
      @Parameter(description = "게시글 제목/내용 검색어")
      @RequestParam(value = "keyword", required = false) String keyword,
      @Parameter(description = "component type code")
      @RequestParam(value = "typeCode", required = false) TypeCode typeCode,
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(
        designBoardManagementFacade.findBoardPreviews(pageable, keyword, typeCode));
  }

  /**
   * 특정 디자인 에셋 게시글 상세 조회
   */
  @GetMapping("/{postId}")
  @Operation(summary = "현재 인증된 사용자가 ID=postId인 특정 디자인 에셋 게시글을 조회한다")
  public ResponseEntity<DesignBoardDetailDto> findDesignBoardDetail(
      @PathVariable Long postId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        designBoardManagementFacade.findBoardDetail(postId, userDetails.getPublicUserId()));
  }

  /**
   * 디자인 에셋 게시글 상세 정보 리스트 조회
   * */
  @GetMapping("/details")
  @Operation(
      summary = "인증된 사용자가 디자인 에셋 게시글 상세 목록을 조회한다",
      description = """
          
          [동작 방식]
          - pinnedPostId가 있는 경우
            - 첫 페이지(page=0)에서 해당 게시글을 최상단에 고정한다.
            - pinnedPostId가 null이 아니라면, pinnedPost 작성자의 게시글만 조회한다.
            - pinnedPost는 page=0일 때만 게시된다.
          - pinnedPostId가 없는 경우
            - 일반적인 페이징 기반 조회로 동작한다.
          """)
  public ResponseEntity<List<DesignBoardDetailDto>> findDesignBoardDetails(
      @Parameter(description = "첫 번째 페이지 최상단에 고정할 게시글 ID")
      @RequestParam(value = "pinnedPostId", required = false) Long pinnedPostId,
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<DesignBoardDetailDto> response = designBoardManagementFacade
        .findBoardDetails(pageable, pinnedPostId, userDetails.getUsername());
    return ResponseEntity.ok(response);
  }

  /**
   * 디자인 에셋 게시글 생성
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 디자인 에셋 게시글을 작성한다")
  public ResponseEntity<DesignBoardDetailDto> createDesignBoard(
      @RequestPart("boardInfo") DesignBoardCreateDto createDto,
      @RequestPart(value = "previewImage", required = false) MultipartFile previewImage,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        designBoardManagementFacade.createDesignBoard(createDto, previewImage,
            userDetails.getUsername()));
  }

  /**
   * 특정 ID의 디자인 에셋 게시글 수정, 없으면 예외 처리
   *
   */
  @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=postId인 디자인 에셋 게시글을 수정한다")
  public ResponseEntity<DesignBoardDetailDto> updateDesignBoard(
      @PathVariable Long postId,
      @RequestPart(value = "boardInfo") DesignBoardUpdateDto updateDto,
      @RequestPart(value = "previewImage", required = false) MultipartFile previewImage,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(designBoardManagementFacade.updateDesignBoard(
        postId,
        updateDto,
        previewImage,
        userDetails.getUsername())
    );
  }

  /**
   * 특정 디자인 에셋 게시글 삭제
   */
  @DeleteMapping("/{postId}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=postId인 디자인 에셋 게시글을 삭제한다")
  public ResponseEntity<DesignBoardDetailDto> deleteDesignBoard(
      @PathVariable Long postId) {
    designBoardManagementFacade.deleteBoardDetailWithPost(postId);
    return ResponseEntity.noContent().build();
  }
}
