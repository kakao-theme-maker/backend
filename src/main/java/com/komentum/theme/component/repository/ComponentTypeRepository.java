package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.enums.Platform;
import java.util.List;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentTypeRepository extends JpaRepository<ComponentType, Integer> {

  List<ComponentType> findByPlatform(Platform platform);
  List<ComponentType> findAllByComponentPathIn(Collection<String> componentPathList);
}
