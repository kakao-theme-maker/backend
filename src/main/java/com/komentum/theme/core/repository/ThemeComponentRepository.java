package com.komentum.theme.core.repository;

import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.enums.ThemeType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeComponentRepository extends JpaRepository<ThemeComponent, Integer> {

  List<ThemeComponent> findByUserEmail(String userEmail, Pageable pageable);

  List<ThemeComponent> findByIsPublicTrue(Pageable pageable);

  List<ThemeComponent> findByIsDoneTrue(Pageable pageable);

  List<ThemeComponent> findByIsDoneTrueAndUserEmail(String userEmail, Pageable pageable);

  List<ThemeComponent> findByUserEmailIn(List<String> userEmail);

  boolean existsByThemeCode(String themeCode);

  List<ThemeComponent> findByThemeType(ThemeType themeType);
}
