package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorStyleRepository extends JpaRepository<ColorStyle, Integer> {

    /**
     * 플랫폼별 ColorStyle 조회
     */
    List<ColorStyle> findByPlatform(Platform platform);

    /**
     * 플랫폼별 ColorStyle 개수 조회
     */
    long countByPlatform(Platform platform);

    /**
     * 플랫폼별 ColorStyle 삭제
     */
    void deleteByPlatform(Platform platform);
}
