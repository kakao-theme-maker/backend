package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  
  @Query(
      "select new com.komentum.post.dto.PostSummary(p, count(distinct pr)) "
          +
          "from Post p " +
          "left join Prefer pr on p = pr.post " +
          "group by p")
  List<PostSummary> getPostSummary(Pageable pageable);

  @Query(
      "select new com.komentum.post.dto.PostSummary(p, count(distinct pr)) "
          +
          "from Post p " +
          "left join Prefer pr on p = pr.post " +
          "where p.postId = :postId " +
          "group by p")
  Optional<PostSummary> getPostSummaryByPostId(@Param("postId") long postId);
}
