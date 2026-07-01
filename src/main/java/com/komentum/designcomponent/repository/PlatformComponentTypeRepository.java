package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlatformComponentTypeRepository extends
    JpaRepository<PlatformComponentType, Long> {

  List<PlatformComponentType> findAllByPlatform(Platform platform);

  @Query("select pc from PlatformComponentType pc join fetch pc.componentType where pc.platform=:platform")
  List<PlatformComponentType> fetchJoinAllByPlatform(Platform platform);
}
