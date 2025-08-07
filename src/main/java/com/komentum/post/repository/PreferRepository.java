package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferRepository extends JpaRepository<Prefer, Long> {

  boolean existsByUserAndPost(User user, Post post);

  void deleteByUserAndPost(User user, Post post);

  Long countPreferByPost_PostId(Long postPostId);
}
