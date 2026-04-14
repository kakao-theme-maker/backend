package com.komentum.post.service;

import com.komentum.post.domain.Comment;
import com.komentum.post.domain.CommentLike;
import com.komentum.post.repository.CommentLikeRepository;
import com.komentum.post.repository.CommentRepository;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final EntityManager entityManager;

  @Transactional
  public void like(Long commentId, Long userId) {
    validateCommentExists(commentId);
    if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
      return;
    }

    Comment comment = entityManager.getReference(Comment.class, commentId);
    User user = entityManager.getReference(User.class, userId);
    try {
      commentLikeRepository.saveAndFlush(CommentLike.createTransient(comment, user));
    } catch (DataIntegrityViolationException e) {
      return;
    }
    commentRepository.increaseLikeCount(commentId);
  }

  @Transactional
  public void unlike(Long commentId, Long userId) {
    validateCommentExists(commentId);
    int deleted = commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
    if (deleted > 0) {
      commentRepository.decreaseLikeCount(commentId);
    }
  }

  @Transactional(readOnly = true)
  public Long getLikeCount(Long commentId) {
    validateCommentExists(commentId);
    return commentRepository.findById(commentId)
        .map(Comment::getLikeCount)
        .orElse(0L);
  }

  @Transactional(readOnly = true)
  public boolean isLiked(Long commentId, Long userId) {
    return commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
  }

  @Transactional(readOnly = true)
  public List<Long> getLikedCommentIds(Long userId, List<Long> commentIds) {
    if (commentIds.isEmpty()) {
      return Collections.emptyList();
    }
    return commentLikeRepository.findByUser_UserIdAndComment_CommentIdIn(userId, commentIds)
        .stream()
        .map(commentLike -> commentLike.getComment().getCommentId())
        .toList();
  }

  private void validateCommentExists(Long commentId) {
    if (!commentRepository.existsById(commentId)) {
      throw new EntityNotFoundException(
          String.format("failed to find comment with id : %d", commentId));
    }
  }
}
