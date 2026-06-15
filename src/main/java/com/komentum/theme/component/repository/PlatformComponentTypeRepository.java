package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.PlatformComponentType;
import com.komentum.theme.component.enums.Platform;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformComponentTypeRepository extends
    JpaRepository<PlatformComponentType, Long> {

  List<PlatformComponentType> findAllByPlatform(Platform platform);
}
