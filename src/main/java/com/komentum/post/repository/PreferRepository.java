package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Prefer;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferRepository extends JpaRepository<Prefer, Long> {

  boolean existsByUserAndPost(User user, Post post);

  Optional<Prefer> findByUserAndPost(User user, Post post);

  void deleteByUserAndPost(User user, Post post);

  Long countPreferByPost_PostId(Long postPostId);

  @Query("select distinct p "
      + "from Prefer p "
      + "join fetch p.user "
      + "join fetch p.post "
      + "where p.post in :posts")
  List<Prefer> fetchJoinByPostIn(@Param("posts") List<Post> posts);
}
