package com.komentum.post.repository;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.theme.component.domain.DesignComponent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignBoardRepository extends JpaRepository<DesignBoard, Long> {

  void deleteByPost_PostId(Long postPostId);

  Optional<DesignBoard> findByPost_PostId(Long postPostId);

  boolean existsByDesignComponentAndPost(DesignComponent designComponent, Post post);

  boolean existsByPost_PostId(Long postPostId);
}
