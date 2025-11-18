package com.komentum.post.repository;

import com.komentum.post.domain.DesignBoard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignBoardRepository extends JpaRepository<DesignBoard, Long> {

  void deleteByPost_PostId(Long postPostId);

  Optional<DesignBoard> findByPost_PostId(Long postPostId);
}
