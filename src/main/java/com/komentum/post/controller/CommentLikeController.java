package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.facade.CommentLikeManagementFacade;
import com.komentum.post.service.CommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {

  private final CommentLikeManagementFacade commentLikeManagementFacade;
  private final CommentLikeService commentLikeService;

  @GetMapping("/{commentId}/like")
  @Operation(summary = "현재 인증된 사용자가 ID=commentId인 댓글의 좋아요 수를 조회한다")
  public ResponseEntity<Long> getLikeCount(@PathVariable Long commentId) {
    return ResponseEntity.ok(commentLikeService.getLikeCount(commentId));
  }

  @PostMapping("/{commentId}/like")
  @Operation(summary = "현재 인증된 사용자가 ID=commentId인 댓글을 좋아요한다")
  public ResponseEntity<Void> like(
      @PathVariable Long commentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    commentLikeManagementFacade.like(commentId, userDetails.getUsername());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{commentId}/like")
  @Operation(summary = "현재 인증된 사용자가 ID=commentId인 댓글 좋아요를 취소한다")
  public ResponseEntity<Void> unlike(
      @PathVariable Long commentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    commentLikeManagementFacade.unlike(commentId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }
}
