package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.domain.Comment;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.facade.CommentManagementFacade;
import com.komentum.post.service.CommentLikeService;
import com.komentum.post.service.CommentService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final CommentLikeService commentLikeService;
  private final UserEntityFinder userEntityFinder;
  private final CommentManagementFacade commentManagementFacade;

  @GetMapping("/{postId}/comments")
  @Operation(summary = "현재 인증된 사용자가 ID=postId인 게시글의 모든 댓글을 조회한다 (현재 사용자 기준 좋아요 여부 포함)")
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId,
      @PageableDefault(size = 20, sort = "createdAt") @ParameterObject Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    User client = userEntityFinder.findUserEntity(userDetails.getUsername());
    List<Comment> comments = commentService.getComments(postId, pageable);
    // 내가 좋아요 누른 댓글 목록
    Set<Long> likedCommentIds = Set.copyOf(commentLikeService.getLikedCommentIds(
        client.getUserId(),
        comments.stream().map(Comment::getCommentId).toList()));
    return ResponseEntity.ok(
        comments.stream()
            .map(comment -> CommentResponse.from(
                comment,
                likedCommentIds.contains(comment.getCommentId())))
            .toList());
  }

  @GetMapping("/comments/{commentId}")
  @Operation(summary = "현재 인증된 사용자가 ID=commentId인 특정 댓글을 조회한다 (현재 사용자 기준 좋아요 여부 포함)")
  public ResponseEntity<CommentResponse> getComment(
      @PathVariable Long commentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    User client = userEntityFinder.findUserEntity(userDetails.getUsername());
    return ResponseEntity.ok(CommentResponse.from(
        commentService.getCommentById(commentId),
        commentLikeService.isLiked(commentId, client.getUserId())));
  }

  @PostMapping("/{postId}/comments")
  @Operation(summary = "현재 인증된 사용자가 ID=postId인 게시글에 댓글을 생성한다")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable Long postId,
      @RequestBody CommentCreateDto createDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        commentManagementFacade.createCommentOnPost(postId, createDto, userDetails.getUsername()));
  }

  @PutMapping("/comments/{commentId}")
  @Operation(summary = "현재 인증된 사용자가 자신이 작성한 ID=commentId인 댓글을 수정한다")
  public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId,
      @RequestBody CommentUpdateDto updateDto) {
    return ResponseEntity.ok(
        CommentResponse.from(commentService.updateComment(commentId, updateDto)));
  }

  @DeleteMapping("/comments/{commentId}")
  @Operation(summary = "현재 인증된 사용자가 자신이 작성한 ID=commentId인 댓글을 삭제한다")
  public ResponseEntity<CommentResponse> deleteComment(@PathVariable Long commentId) {
    commentService.deleteComment(commentId);
    return ResponseEntity.noContent().build();
  }

  // public ResponseEntity<UserInquiryResponseDto<UserResponseDto>>
  // dto. from
  // data + PostService.countPost + UserEntityFinder.countsubs---


}
