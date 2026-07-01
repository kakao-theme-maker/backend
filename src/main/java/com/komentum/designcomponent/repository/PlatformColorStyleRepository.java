package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.enums.Platform;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformColorStyleRepository extends JpaRepository<PlatformColorStyle, Long> {

  @Query("select pcs from PlatformColorStyle pcs join fetch pcs.colorStyle where pcs.platform=:platform")
  List<PlatformColorStyle> fetchJoinAllByPlatform(Platform platform);
}
