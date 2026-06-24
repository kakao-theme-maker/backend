package com.komentum.theme.theme.repository;

import com.komentum.theme.theme.domain.ThemeStyle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeStyleRepository extends JpaRepository<ThemeStyle, Integer> {

  @Query("select ts from ThemeStyle ts join fetch ThemeComponent tc where tc.themeComponentId = :themeComponentId")
  List<ThemeStyle> findByThemeComponentId(
      @Param("themeComponentId") Integer themeComponentId); // component 리스트 반환

  @Modifying
  @Query("DELETE FROM ThemeStyle ts WHERE ts.themeComponent.themeComponentId = :themeComponentId")
  void deleteByThemeComponentId(
      @Param("themeComponentId") Integer themeComponentId); // component 삭제

  @Query("SELECT ts FROM ThemeStyle ts "
      + "JOIN FETCH ts.themeComponent tc "
      + "JOIN FETCH ts.colorStyle cs "
      + "WHERE tc.themeComponentId = :themeComponentId")
  List<ThemeStyle> fetchJoinAllByThemeComponentId(Integer themeComponentId);
}