package com.komentum.post.repository;

import com.komentum.post.domain.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findAllByPost_PostId(Long postPostId);

  @Query("select t from Tag t where t.tagId in :postIds")
  List<Tag> findAllByPostIds(@Param("postIds") List<Long> postIds);

  @Query("select t from Tag t " +
      "join fetch t.post p " +
      "where t.post.postId in :postIds")
  List<Tag> fetchJoinAllByPostIds(@Param("postIds") List<Long> postIds);
}
