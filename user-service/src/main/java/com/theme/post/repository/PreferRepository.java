package com.theme.post.repository;

import com.theme.domain.User;
import com.theme.post.domain.Post;
import com.theme.post.domain.Prefer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferRepository extends JpaRepository<Prefer, Long> {

  boolean existsByUserAndPost(User user, Post post);

  void deleteByUserAndPost(User user, Post post);

  Long countPreferByPost_PostId(Long postPostId);
}
