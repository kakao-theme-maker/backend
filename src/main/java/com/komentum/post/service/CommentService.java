package com.komentum.post.service;

import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.repository.CommentRepository;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  public List<Comment> getComments(Long postId, Pageable pageable) {
    return commentRepository.findAllByPost_PostId(postId, pageable);
  }

  public Comment getCommentById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Comment not found"));
  }

  @Transactional
  public Comment saveComment(Post post, User author, CommentCreateDto commentCreateDto) {
    Comment comment = Comment.createTransient(commentCreateDto, post, author);
    return commentRepository.save(comment);
  }

  @Transactional
  public Comment updateComment(Long commentId, CommentUpdateDto commentUpdateDto) {
    Comment comment = getCommentById(commentId);
    comment.update(commentUpdateDto);
    return commentRepository.save(comment);
  }

  @Transactional
  public void deleteComment(Long commentId) {
    Comment targetComment = getCommentById(commentId);
    commentRepository.deleteById(targetComment.getCommentId());
  }
}
