package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  @Query("select p, count(pr) from Post p left join Prefer pr on p=pr.post group by p")
  List<Object[]> getPreferPosts(Pageable pageable);

  @Query("select p, count(pr) from Post p left join Prefer pr on p=pr.post where p.postId=:postId group by p")
  Object getPreferPostsByPostId(@Param("postId") long postId);
}
