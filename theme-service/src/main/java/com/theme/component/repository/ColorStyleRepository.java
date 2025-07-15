package com.theme.repository;

import com.theme.domain.ColorStyle;
import org.springframework.data.jpa.repository.JpaRepository; //JPA 기본 repository 인터페이스
import org.springframework.stereotype.Repository; // 인터페이스가 repository 역할을 함을 나타냄

@Repository
public interface ColorStyleRepository extends JpaRepository<ColorStyle, Integer> {
}
