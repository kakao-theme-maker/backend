package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardUpdateDto;
import com.komentum.post.facade.DesignBoardManagementFacade;
import io.swagger.v3.oas.annotations.Operation;
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
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(
        designBoardManagementFacade.findBoardPreviews(pageable));
  }

  /**
   * 특정 디자인 에셋 게시글 상세 조회
   */
  @GetMapping("/{post_id}")
  @Operation(summary = "현재 인증된 사용자가 ID=post_id인 특정 디자인 에셋 게시글을 조회한다")
  public ResponseEntity<DesignBoardDetailDto> findDesignBoardDetail(
      @PathVariable("post_id") Long postId
  ) {
    return ResponseEntity.ok(designBoardManagementFacade.findBoardDetail(postId));
  }

  /**
   * 디자인 에셋 게시글 생성
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 디자인 에셋 게시글을 작성한다")
  public ResponseEntity<DesignBoardDetailDto> createDesignBoard(
      @RequestPart("board_info") DesignBoardCreateDto createDto,
      @RequestPart(value = "preview_image", required = false) MultipartFile previewImage,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        designBoardManagementFacade.createDesignBoard(createDto, previewImage,
            userDetails.getUsername()));
  }

  /**
   * 특정 ID의 디자인 에셋 게시글 수정, 없으면 예외 처리
   * */
  @PatchMapping(value = "/{post_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=post_id인 디자인 에셋 게시글을 수정한다")
  public ResponseEntity<DesignBoardDetailDto> updateDesignBoard(
      @PathVariable("post_id") Long postId,
      @RequestPart(value = "board_info") DesignBoardUpdateDto updateDto,
      @RequestPart(value = "preview_image", required = false) MultipartFile profileImage
  ) {
    return ResponseEntity.ok(
        designBoardManagementFacade.updateDesignBoard(postId, updateDto, profileImage));
  }

  /**
   * 특정 디자인 에셋 게시글 삭제
   */
  @DeleteMapping("/{post_id}")
  @Operation(summary = "현재 인증된 사용자가 자신이 소유한 ID=post_id인 디자인 에셋 게시글을 삭제한다")
  public ResponseEntity<DesignBoardDetailDto> deleteDesignBoard(
      @PathVariable("post_id") Long postId) {
    designBoardManagementFacade.deleteBoardDetailWithPost(postId);
    return ResponseEntity.noContent().build();
  }
}
