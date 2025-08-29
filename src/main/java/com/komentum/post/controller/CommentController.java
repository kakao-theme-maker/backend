package com.komentum.post.controller;

import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @GetMapping("/{postId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId,
      @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
      @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
    return ResponseEntity.ok(
        commentService.getComments(postId, pageNumber, pageSize).stream().map(CommentResponse::from)
            .toList());
  }

  @GetMapping("/comments/{commentId}")
  public ResponseEntity<CommentResponse> getComment(@PathVariable Long commentId) {
    return ResponseEntity.ok(CommentResponse.from(commentService.getCommentById(commentId)));
  }

  @PostMapping("/comments")
  public ResponseEntity<CommentResponse> createComment(
      @RequestBody CommentCreateDto createDto) {
    return ResponseEntity.ok(CommentResponse.from(commentService.saveComment(createDto)));
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
