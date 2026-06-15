package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.PlatformColorStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformColorStyleRepository extends JpaRepository<PlatformColorStyle, Long> {

}
