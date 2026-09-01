package com.komentum.theme.core.repository;

import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.enums.ThemeType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select theme from ThemeComponent theme where theme.themeComponentId = :id")
  Optional<ThemeComponent> findByIdForUpdate(@Param("id") Integer id);
}
