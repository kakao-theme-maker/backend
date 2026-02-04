package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.dto.PageableRequestDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.facade.ThemeBoardManagementFacade;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
   * @param pageableRequestDto 조회할 페이지 정보
   * @return 조회한 게시글 목록 반환
   * */
  @GetMapping
  public ResponseEntity<List<ThemeBoardPreviewDto>> findThemeBoards(
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.findThemeBoardPreviews(pageableRequestDto.toPageable()));
  }

  /**
   * 게시글 ID를 기반으로 테마 게시글 상세 조회
   * @param postId 게시글 ID
   * @return 게시글 상세 정보 반환
   * */
  @GetMapping("/{post_id}")
  public ResponseEntity<ThemeBoardDetailDto> findThemeBoardByPostId(
      @PathVariable("post_id") Long postId) {
    return ResponseEntity.ok(themeBoardManagementFacade.findThemeBoardDetail(postId));
  }

  /**
   * 현재 사용자가 테마 게시글 생성
   * @param createDto 게시글 생성 정보
   * @param profileImage 게시글 프로필 이미지 정보
   * */
  @PostMapping
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
   * @param postId 수정할 게시글 ID
   * @param updateDto 게시글 수정 정보
   * */
  @PutMapping("/{post_id}")
  public ResponseEntity<ThemeBoardDetailDto> updatePost(@PathVariable("post_id") Long postId,
      @RequestBody ThemeBoardUpdateDto updateDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.updateThemeBoard(postId, updateDto));
  }

  /**
   * 현재 사용자가 테마 게시글 삭제
   * @param postId 삭제할 게시글 ID
   * */
  @DeleteMapping("/{post_id}")
  public ResponseEntity<Void> deletePost(@PathVariable("post_id") Long postId) {
    themeBoardManagementFacade.deleteThemeBoard(postId);
    return ResponseEntity.noContent().build();
  }
}
