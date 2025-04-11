package com.theme.repository;

import com.theme.domain.ThemeStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeStyleRepository extends JpaRepository<ThemeStyle, Integer> {
    List<ThemeStyle> findByThemeComponentId(Integer themeComponentId); // component 리스트 반환

    @Modifying
    @Query("DELETE FROM ThemeStyle ts WHERE ts.themeComponentId = :themeComponentId")
    void deleteByThemeComponentId(@Param("themeComponentId") Integer themeComponentId); // component 삭제
}
