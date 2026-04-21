package com.komentum.post.service;

import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.policy.CommentPolicy;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.repository.CommentRepository;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentPolicy commentPolicy;

  @Transactional(readOnly = true)
  public List<Comment> getComments(Long postId, Pageable pageable) {
    return commentRepository.findAllByPost_PostId(postId, pageable);
  }

  @Transactional(readOnly = true)
  public Comment getCommentById(Long commentId) {
    return commentRepository.findWithUserByCommentId(commentId)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("failed to find post with id : %d", commentId)));
  }

  @Transactional
  public Comment saveComment(Post post, User author, CommentCreateDto commentCreateDto) {
    Comment comment = Comment.createTransient(commentCreateDto, post, author);
    return commentRepository.save(comment);
  }

  @Transactional
  public Comment updateComment(Long commentId, CommentUpdateDto commentUpdateDto) {
    Comment comment = getCommentById(commentId);
    if (!commentPolicy.canUpdate(comment.getUser())) {
      throw new AccessDeniedException("failed to update comment : invalid user or role");
    }
    comment.update(commentUpdateDto);
    return commentRepository.save(comment);
  }

  @Transactional
  public void deleteComment(Long commentId) {
    Comment targetComment = getCommentById(commentId);
    if (!commentPolicy.canDelete(targetComment.getUser())) {
      throw new AccessDeniedException("failed to delete comment : invalid user or role");
    }
    commentRepository.deleteById(targetComment.getCommentId());
  }
}
