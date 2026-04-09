package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.facade.BookmarkManagementFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

  private final BookmarkManagementFacade bookmarkManagementFacade;

  @PutMapping("/posts/{post_id}")
  @Operation(summary = "게시글을 북마크에 추가하고, 게시글이 이미 북마크에 존재한다면 현재 상태를 유지한다")
  public ResponseEntity<Void> addPostOnBookmark(
      @Parameter(description = "북마크에 추가할 게시글 ID")
      @PathVariable("post_id") Long postId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    bookmarkManagementFacade.addPostOnBookmark(postId, userDetails.getUsername());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/posts/{post_id}")
  @Operation(summary = "게시글을 북마크에서 제거하고, 게시글이 북마크에 존재하지 않다면 현재 상태를 유지한다")
  public ResponseEntity<Void> deletePostFromBookmark(
      @Parameter(description = "북마크에서 제거할 게시글 ID")
      @PathVariable("post_id") Long postId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    bookmarkManagementFacade.deletePostFromBookmark(postId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }
}
