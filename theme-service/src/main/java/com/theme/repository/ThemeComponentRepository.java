package com.theme.repository;

import com.theme.domain.ThemeComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeComponentRepository extends JpaRepository<ThemeComponent, Integer> {
    List<ThemeComponent> findByUserEmail(String userEmail); //user 이메일로 검색
    List<ThemeComponent> findByIsPublicTrue(); // 공개 검색
    List<ThemeComponent> findByIsDoneTrue();
    List<ThemeComponent> findByUserEmailAndIsDoneTrue(String userEmail);

}
