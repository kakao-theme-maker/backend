package com.komentum.post.repository;

import com.komentum.post.domain.Comment;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findAllByPost_PostId(Long postPostId, Pageable pageable);
}
