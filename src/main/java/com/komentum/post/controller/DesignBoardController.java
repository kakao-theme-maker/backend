package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.dto.PageableRequestDto;
import com.komentum.post.dto.DesignBoardDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardDetailDto;
import com.komentum.post.dto.DesignBoardDto.DesignBoardPreviewDto;
import com.komentum.post.facade.DesignBoardManagementFacade;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
   * */
  @GetMapping
  public ResponseEntity<List<DesignBoardPreviewDto>> findDesignBoards(
      @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        designBoardManagementFacade.findBoardPreviews(pageableRequestDto.toPageable()));
  }

  /**
   * 특정 디자인 에셋 게시글 상세 조회
   * */
  @GetMapping("/{post_id}")
  public ResponseEntity<DesignBoardDetailDto> findDesignBoardDetail(
      @PathVariable("post_id") Long postId
  ) {
    return ResponseEntity.ok(designBoardManagementFacade.findBoardDetail(postId));
  }

  /**
   * 디자인 에셋 게시글 생성
   * */
  @PostMapping
  public ResponseEntity<DesignBoardDetailDto> createDesignBoard(
      @RequestPart("board_info") DesignBoardCreateDto createDto,
      @RequestPart("preview_image") MultipartFile profileImage,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        designBoardManagementFacade.createDesignBoard(createDto, profileImage,
            userDetails.getUsername()));
  }

  /**
   * 특정 ID의 디자인 에셋 게시글 수정, 없으면 예외 처리
   * */
  @PatchMapping("/{post_id}")
  public ResponseEntity<DesignBoardDetailDto> updateDesignBoard(
      @PathVariable("post_id") Long postId,
      @RequestBody DesignBoardDto.DesignBoardUpdateDto updateDto
  ) {
    return ResponseEntity.ok(designBoardManagementFacade.updateDesignBoard(postId, updateDto));
  }

  /**
   * 특정 디자인 에셋 게시글 삭제
   * */
  @DeleteMapping("/{post_id}")
  public ResponseEntity<DesignBoardDetailDto> deleteDesignBoard(
      @PathVariable("post_id") Long postId) {
    designBoardManagementFacade.deleteBoardDetailWithPost(postId);
    return ResponseEntity.noContent().build();
  }
}
