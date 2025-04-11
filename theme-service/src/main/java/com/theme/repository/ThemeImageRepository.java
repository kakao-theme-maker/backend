package com.theme.repository;

import com.theme.domain.ThemeImage;
import com.theme.domain.ThemeImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeImageRepository extends JpaRepository<ThemeImage, ThemeImageId> {
    @Query("SELECT ti FROM ThemeImage ti WHERE ti.id.themeComponentId = :themeComponentId")
    List<ThemeImage> findByThemeComponentId(@Param("themeComponentId") Integer themeComponentId); //theme Image 객체 리스트 반환

    @Modifying
    @Query("DELETE FROM ThemeImage ti WHERE ti.id.themeComponentId = :themeComponentId")
    void deleteByThemeComponentId(@Param("themeComponentId") Integer themeComponentId); // theme Image 엔티티 삭제
}
