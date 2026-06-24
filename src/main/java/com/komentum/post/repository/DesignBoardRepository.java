package com.komentum.post.repository;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.designcomponent.domain.DesignComponent;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignBoardRepository extends JpaRepository<DesignBoard, Long> {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  void deleteByPost_PostId(Long postPostId);

  void deleteByPostAndDesignComponentIn(Post post, List<DesignComponent> designComponents);

  List<DesignBoard> findByPost_PostId(Long postPostId);

  @EntityGraph(attributePaths = {"designComponent"})
  List<DesignBoard> findWithDesignComponentByPost(Post post);

  boolean existsByDesignComponentAndPost(DesignComponent designComponent, Post post);

  boolean existsByPost_PostId(Long postPostId);

  @EntityGraph(attributePaths = {"designComponent"})
  List<DesignBoard> findWithDesignComponentByPost_PostIdIn(List<Long> postIds);
}
