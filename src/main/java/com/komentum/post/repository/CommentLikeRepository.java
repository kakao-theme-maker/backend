package com.komentum.post.repository;

import com.komentum.post.domain.CommentLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

  boolean existsByUser_UserIdAndComment_CommentId(Long userId, Long commentId);

  long deleteByUser_UserIdAndComment_CommentId(Long userId, Long commentId);

  List<CommentLike> findByUser_UserIdAndComment_CommentIdIn(
      Long userId,
      List<Long> commentIds);

  default boolean existsByUserIdAndCommentId(Long userId, Long commentId) {
    return existsByUser_UserIdAndComment_CommentId(userId, commentId);
  }

  default int deleteByUserIdAndCommentId(Long userId, Long commentId) {
    return (int) deleteByUser_UserIdAndComment_CommentId(userId, commentId);
  }

}
