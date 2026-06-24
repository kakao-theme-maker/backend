package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformColorStyleRepository extends JpaRepository<PlatformColorStyle, Long> {

}
