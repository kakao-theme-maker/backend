package com.komentum.theme.theme.repository;

import com.komentum.theme.theme.domain.ThemeComponent;
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
}
