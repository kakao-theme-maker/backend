package com.komentum.post.controller;

import com.komentum.global.dto.PageableRequestDto;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.facade.CommentManagementFacade;
import com.komentum.post.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final CommentManagementFacade commentManagementFacade;

  @GetMapping("/{postId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId,
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        commentService.getComments(postId, pageableRequestDto.toPageable())
            .stream()
            .map(CommentResponse::from)
            .toList());
  }

  @GetMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> getComment(@PathVariable Long commentId) {
    return ResponseEntity.ok(CommentResponse.from(commentService.getCommentById(commentId)));
  }

  @PostMapping("/{postId}/comments")
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable Long postId,
      @RequestBody CommentCreateDto createDto) {
    return ResponseEntity.ok(commentManagementFacade.createCommentOnPost(postId, createDto));
  }

  @PutMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> updateComment(@PathVariable Long commentId,
      @RequestBody CommentUpdateDto updateDto) {
    return ResponseEntity.ok(
        CommentResponse.from(commentService.updateComment(commentId, updateDto)));
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> deleteComment(@PathVariable Long commentId) {
    commentService.deleteComment(commentId);
    return ResponseEntity.noContent().build();
  }
}
