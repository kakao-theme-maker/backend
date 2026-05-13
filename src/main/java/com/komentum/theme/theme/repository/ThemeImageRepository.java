package com.komentum.theme.theme.repository;

import com.komentum.theme.component.enums.TypeCode;
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

  @Query("SELECT ti FROM ThemeImage ti "
      + "JOIN FETCH ti.themeComponent tc "
      + "JOIN FETCH ti.componentType ct "
      + "JOIN FETCH ti.designComponent dc "
      + "WHERE tc.themeComponentId IN :themeComponentIds "
      + "AND ti.componentType.typeCode = :typeCode")
  List<ThemeImage> fetchJoinByThemeComponentAndTypeCode(
      List<Integer> themeComponentIds, TypeCode typeCode);
}
