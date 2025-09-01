package com.komentum.theme.theme.repository;

import com.komentum.theme.theme.domain.ThemeComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeComponentRepository extends JpaRepository<ThemeComponent, Integer> {

  @Query("select distinct tc from ThemeComponent tc "
      + "join fetch ThemeStyle ts on tc=ts.themeComponent "
      + "join fetch ThemeImage ti on tc=ti.themeComponent "
      + "where tc.userEmail=:userEmail")
  List<ThemeComponent> fetchJoinByUserEmail(@Param("userEmail") String userEmail); //user 이메일로 검색

  @Query("select distinct tc from ThemeComponent tc "
      + "join fetch ThemeStyle ts on tc=ts.themeComponent "
      + "join fetch ThemeImage ti on tc=ti.themeComponent ")
  List<ThemeComponent> fetchJoinAll();

  @Query("select distinct tc from ThemeComponent tc "
      + "join fetch ThemeStyle ts on tc=ts.themeComponent "
      + "join fetch ThemeImage ti on tc=ti.themeComponent "
      + "where tc.isPublic=true ")
  List<ThemeComponent> fetchJoinByIsPublicTrue(); // 공개 검색

  @Query("select distinct tc from ThemeComponent tc "
      + "join fetch ThemeStyle ts on tc=ts.themeComponent "
      + "join fetch ThemeImage ti on tc=ti.themeComponent "
      + "where tc.isDone=true ")
  List<ThemeComponent> fetchJoinByIsDoneTrue();

  @Query("select distinct tc from ThemeComponent tc "
      + "join fetch ThemeStyle ts on tc=ts.themeComponent "
      + "join fetch ThemeImage ti on tc=ti.themeComponent "
      + "where tc.isDone=true and tc.userEmail=:userEmail")
  List<ThemeComponent> fetchJoinByUserEmailAndIsDoneTrue(@Param("userEmail") String userEmail);
}
