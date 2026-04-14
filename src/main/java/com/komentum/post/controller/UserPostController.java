package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.facade.PostManagementFacade;
import com.komentum.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPostController {

  private final PostService postService;
  private final PostManagementFacade postManagementFacade;

  // user post dto 리스트 -> 커스텀 응답
  @GetMapping("/me/upload-posts")
  @Operation(summary = "현재 인증된 사용자가 업로드/소유한 게시글 목록을 조회한다")
  public ResponseEntity<List<UserPostListResponseDto>> findUserPostList(@AuthenticationPrincipal
  CustomUserDetails userDetails) {
    return ResponseEntity.ok(postManagementFacade.findMyPostsByUser(userDetails.getUsername()));
  }

  /**
   * 사용자가 카테고리에 저장한 테마 목록 반환
   */
  @GetMapping("/me/bookmarked-posts")
  @Operation(summary = "현재 인증된 사용자가 북마크에 추가한 게시글 목록을 조회한다")
  public ResponseEntity<List<UserPostListResponseDto>> findSavedPostList(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        postManagementFacade.findUserSavedPostsByCategory(userDetails.getUsername()));
  }

  @GetMapping("/me/preferred-posts")
  @Operation(summary = "현재 인증된 사용자가 좋아요를 누른 게시글 목록을 조회한다")
  public ResponseEntity<List<UserPostListResponseDto>> findPreferredPostList(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        postManagementFacade.findUserPreferredPosts(userDetails.getUsername()));
  }
}
