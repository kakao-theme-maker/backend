package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformComponentTypeRepository extends
    JpaRepository<PlatformComponentType, Long> {

  List<PlatformComponentType> findAllByPlatform(Platform platform);
}
