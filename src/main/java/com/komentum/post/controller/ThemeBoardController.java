package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.facade.ThemeBoardManagementFacade;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/theme-boards")
@RequiredArgsConstructor
public class ThemeBoardController {

  private final ThemeBoardManagementFacade themeBoardManagementFacade;

  /**
   * 테마 게시글 목록을 페이지 기반으로 조회
   *
   * @param pageable 조회할 페이지 정보
   * @return 조회한 게시글 목록 반환
   */
  @GetMapping
  @Operation(summary = "인증된 사용자가 테마 게시글 목록을 조회한다")
  public ResponseEntity<List<ThemeBoardPreviewDto>> findThemeBoards(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.findThemeBoardPreviews(pageable));
  }

  /**
   * 인기 테마 게시글 목록 반환
   */
  @GetMapping("/popular")
  @Operation(summary = "인증된 사용자가 인기 테마 게시글 목록을 조회한다")
  public ResponseEntity<List<ThemeBoardPreviewDto>> findPopularThemeBoards(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.findPopularThemeBoardPreviews(
            pageable));
  }

  /**
   * 추천 테마 게시글 목록 반환
   */
  @GetMapping("/recommended")
  @Operation(summary = "인증된 사용자가 추천 테마 게시글 목록을 조회한다 ( 현재 임시 데이터 제공중 )")
  public ResponseEntity<List<ThemeBoardPreviewDto>> findRecommendedThemeBoards(
      @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.findRecommendedThemeBoardPreviews(
            pageable));
  }

  /**
   * 게시글 ID를 기반으로 테마 게시글 상세 조회
   *
   * @param postId 게시글 ID
   * @return 게시글 상세 정보 반환
   */
  @GetMapping("/{post_id}")
  @Operation(summary = "인증된 사용자가 ID=post_id인 게시글을 상세 조회한다")
  public ResponseEntity<ThemeBoardDetailDto> findThemeBoardByPostId(
      @PathVariable("post_id") Long postId) {
    return ResponseEntity.ok(themeBoardManagementFacade.findThemeBoardDetail(postId));
  }

  /**
   * 현재 사용자가 테마 게시글 생성
   *
   * @param createDto    게시글 생성 정보
   * @param profileImage 게시글 프로필 이미지 정보
   */
  @PostMapping
  @Operation(summary = "인증된 사용자가 테마 게시글을 생성한다")
  public ResponseEntity<ThemeBoardDetailDto> createPost(
      @RequestPart("board_info") ThemeBoardCreateDto createDto,
      @RequestPart("preview_image") MultipartFile profileImage,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.createThemeBoard(createDto, profileImage,
            userDetails.getUsername()));
  }

  /**
   * 현재 사용자가 테마 게시글 수정
   *
   * @param postId    수정할 게시글 ID
   * @param updateDto 게시글 수정 정보
   */
  @PutMapping("/{post_id}")
  @Operation(summary = "인증된 사용자가 자신이 소유한 ID=post_id인 테마 게시글을 수정한다")
  public ResponseEntity<ThemeBoardDetailDto> updatePost(@PathVariable("post_id") Long postId,
      @RequestBody ThemeBoardUpdateDto updateDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.updateThemeBoard(postId, updateDto));
  }

  /**
   * 현재 사용자가 테마 게시글 삭제
   *
   * @param postId 삭제할 게시글 ID
   */
  @DeleteMapping("/{post_id}")
  @Operation(summary = "인증된 사용자가 자신이 소유한 ID=post_id인 테마 게시글을 삭제한다")
  public ResponseEntity<Void> deletePost(@PathVariable("post_id") Long postId) {
    themeBoardManagementFacade.deleteThemeBoard(postId);
    return ResponseEntity.noContent().build();
  }
}
