package com.komentum.theme.theme.repository;

import com.komentum.theme.theme.domain.ThemeComponent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeComponentRepository extends JpaRepository<ThemeComponent, Integer> {

  List<ThemeComponent> findByUserEmail(String userEmail); //user 이메일로 검색

  List<ThemeComponent> findByIsPublicTrue(); // 공개 검색

  List<ThemeComponent> findByIsDoneTrue();

  List<ThemeComponent> findByUserEmailAndIsDoneTrue(String userEmail);

}
