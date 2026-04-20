package com.komentum.post.repository;

import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  @EntityGraph(attributePaths = "user")
  List<Comment> findAllByPost_PostId(Long postPostId, Pageable pageable);

  @EntityGraph(attributePaths = "user")
  Optional<Comment> findWithUserByCommentId(Long commentId);

  List<Comment> findByPostIn(List<Post> posts);

  @Modifying(flushAutomatically = true)
  @Query("update Comment c set c.likeCount = c.likeCount + 1 where c.commentId = :commentId")
  int increaseLikeCount(@Param("commentId") Long commentId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update Comment c set c.likeCount = c.likeCount - 1 "
      + "where c.commentId = :commentId and c.likeCount > 0")
  int decreaseLikeCount(@Param("commentId") Long commentId);
}
