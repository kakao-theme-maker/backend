package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.facade.PostManagementFacade;
import com.komentum.post.service.PostService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPostController {

  private final PostService postService;
  private final PostManagementFacade postManagementFacade;

  /**
   * 현재 사용자가 소유한 게시글 목록에 디자인 게시글의 활성 에셋 노출 정책을 적용해 반환한다.
   */
  @GetMapping("/me/upload-posts")
  @Operation(
      summary = "현재 인증된 사용자가 업로드/소유한 게시글 목록을 조회한다",
      description = "활성 에셋이 없는 DESIGN_BOARD는 제외하고, 일부만 비활성인 디자인 게시글의 대표 이미지는 ID가 가장 작은 활성 에셋의 이미지로 대체합니다. "
          + "THEME_BOARD는 연결된 에셋의 활성 여부와 관계없이 조회됩니다.")
  public ResponseEntity<List<UserPostListResponseDto>> findUserPostList(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "postType", required = false) PostType postType,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(
        postManagementFacade.findMyPostsByUser(userDetails.getUsername(), postType, pageable));
  }

  /**
   * 현재 사용자의 북마크 목록에 디자인 게시글의 활성 에셋 노출 정책을 적용해 반환한다.
   */
  @Hidden
  @GetMapping("/me/bookmarked-posts")
  @Operation(
      summary = "현재 인증된 사용자가 북마크에 추가한 게시글 목록을 조회한다",
      description = "활성 에셋이 없는 DESIGN_BOARD는 제외하고, 일부만 비활성인 디자인 게시글의 대표 이미지는 ID가 가장 작은 활성 에셋의 이미지로 대체합니다. "
          + "THEME_BOARD는 연결된 에셋의 활성 여부와 관계없이 조회됩니다.")
  public ResponseEntity<List<UserPostListResponseDto>> findBookmarkedPostList(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "postType", required = false) PostType postType,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(
        postManagementFacade.findBookmarkedPostsByUser(userDetails.getUsername(), postType,
            pageable));
  }

  /**
   * 현재 사용자의 좋아요 목록에 디자인 게시글의 활성 에셋 노출 정책을 적용해 반환한다.
   */
  @GetMapping("/me/preferred-posts")
  @Operation(
      summary = "현재 인증된 사용자가 좋아요를 누른 게시글 목록을 조회한다",
      description = "활성 에셋이 없는 DESIGN_BOARD는 제외하고, 일부만 비활성인 디자인 게시글의 대표 이미지는 ID가 가장 작은 활성 에셋의 이미지로 대체합니다. "
          + "THEME_BOARD는 연결된 에셋의 활성 여부와 관계없이 조회됩니다.")
  public ResponseEntity<List<UserPostListResponseDto>> findPreferredPostList(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "postType", required = false) PostType postType,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable
  ) {
    return ResponseEntity.ok(
        postManagementFacade.findUserPreferredPosts(userDetails.getUsername(), postType, pageable));
  }
}
