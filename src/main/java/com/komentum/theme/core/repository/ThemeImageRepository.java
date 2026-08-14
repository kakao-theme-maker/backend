package com.komentum.theme.core.repository;

import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeImageId;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeImageRepository extends JpaRepository<ThemeImage, ThemeImageId> {

  @Modifying
  @Query("DELETE FROM ThemeImage ti WHERE ti.themeComponent.themeComponentId = :themeComponentId")
  void deleteByThemeComponentId(
      @Param("themeComponentId") Integer themeComponentId); // theme Image 엔티티 삭제

  @Modifying
  @Query("""
      DELETE FROM ThemeImage ti
      WHERE ti.themeComponent.themeComponentId = :themeComponentId
        AND ti.componentType.typeCode = :typeCode
      """)
  void deleteByThemeComponentIdAndTypeCode(Integer themeComponentId, TypeCode typeCode);

  @Query("SELECT ti FROM ThemeImage ti "
      + "JOIN FETCH ti.themeComponent tc "
      + "JOIN FETCH ti.componentType ct "
      + "JOIN FETCH ti.designComponent dc "
      + "WHERE tc.themeComponentId IN :themeComponentIds "
      + "AND ti.componentType.typeCode = :typeCode")
  List<ThemeImage> fetchJoinByThemeComponentAndTypeCode(
      List<Integer> themeComponentIds, TypeCode typeCode);

  @Query("SELECT ti "
      + "FROM ThemeImage ti "
      + "JOIN FETCH ti.themeComponent tc "
      + "JOIN FETCH ti.designComponent dc "
      + "JOIN FETCH ti.componentType ct "
      + "WHERE tc.themeComponentId in :themeComponentId")
  List<ThemeImage> fetchJoinAllByThemeComponentIds(Collection<Integer> themeComponentId);

  @Query("SELECT ti "
      + "FROM ThemeImage ti "
      + "JOIN FETCH ti.themeComponent tc "
      + "JOIN FETCH ti.designComponent dc "
      + "JOIN FETCH ti.componentType ct "
      + "WHERE tc.themeComponentId = :themeComponentId")
  List<ThemeImage> fetchJoinAllByThemeComponentId(Integer themeComponentId);
}
