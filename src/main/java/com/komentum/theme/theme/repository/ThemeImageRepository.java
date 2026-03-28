package com.komentum.theme.theme.repository;

import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.domain.ThemeImageId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeImageRepository extends JpaRepository<ThemeImage, ThemeImageId> {

  @Query("SELECT ti FROM ThemeImage ti WHERE ti.themeComponent.themeComponentId = :themeComponentId")
  List<ThemeImage> findByThemeComponentId(
      @Param("themeComponentId") Integer themeComponentId); //theme Image 객체 리스트 반환

  @Modifying
  @Query("DELETE FROM ThemeImage ti WHERE ti.themeComponent.themeComponentId = :themeComponentId")
  void deleteByThemeComponentId(
      @Param("themeComponentId") Integer themeComponentId); // theme Image 엔티티 삭제

  List<ThemeImage> findByThemeComponentAndComponentType_ComponentName(
      ThemeComponent themeComponent,
      String componentTypeComponentName);
}
