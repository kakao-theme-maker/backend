package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.DesignComponent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignComponentRepository extends JpaRepository<DesignComponent, Integer> {

  // 기본 조회 메서드들
  Optional<DesignComponent> findByDesignComponentId(Integer id);

  List<DesignComponent> findByUserEmail(String email);

  List<DesignComponent> findByIsPublic(Boolean isPublic);
}

