package com.komentum.post.repository;

import com.komentum.post.domain.ThemeBoard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeBoardRepository extends JpaRepository<ThemeBoard, Long> {

  Optional<ThemeBoard> findByPost_PostId(Long postPostId);

  void deleteByPost_PostId(Long postPostId);

  void deleteByThemeComponent_ThemeComponentId(Integer themeComponentId);

  @Query("select tb "
      + "from ThemeBoard tb "
      + "join fetch ThemeComponent tc "
      + "where tb.post.postId in :postIds")
  List<ThemeBoard> findAllByPostIds(List<Long> postIds);
}
