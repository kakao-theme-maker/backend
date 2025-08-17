package com.komentum.theme.theme.repository;

import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.theme.theme.domain.ThemeStyleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ThemeStyleRepository extends JpaRepository<ThemeStyle, ThemeStyleId> {

  List<ThemeStyle> findByThemeComponentId(Integer themeComponentId); // component 리스트 반환

  @Modifying
  @Query("DELETE FROM ThemeStyle ts WHERE ts.themeComponentId = :themeComponentId")
  void deleteByThemeComponentId(
      @Param("themeComponentId") Integer themeComponentId); // component 삭제

  @Modifying
  @Transactional
  @Query("DELETE FROM ThemeStyle ts WHERE ts.themeComponentId = :themeComponentId AND ts.cssSelector IS NOT NULL")
  void deleteByThemeComponentIdAndCssSelectorIsNotNull(
      @Param("themeComponentId") Integer themeComponentId); // CSS 커스터마이징 데이터만 삭제
}
