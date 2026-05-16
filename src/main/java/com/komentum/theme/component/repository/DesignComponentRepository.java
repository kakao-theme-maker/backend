package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.DesignComponent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DesignComponentRepository extends JpaRepository<DesignComponent, Integer> {

  @EntityGraph(attributePaths = {"user", "componentTypeMappings",
      "componentTypeMappings.componentType"})
  Optional<DesignComponent> findByDesignComponentId(Integer designComponentId);

  @Query("select dc.designComponentId from DesignComponent dc")
  Page<Integer> findDesignComponentIdPage(Pageable pageable);

  @Query("""
      select distinct dc.designComponentId
      from DesignComponent dc
      join dc.componentTypeMappings componentTypeMapping
      where componentTypeMapping.componentType.componentTypeId = :componentTypeId
      """)
  List<Integer> findDesignComponentIdsByComponentTypeId(
      @Param("componentTypeId") Integer componentTypeId);

  @EntityGraph(attributePaths = {"user", "componentTypeMappings",
      "componentTypeMappings.componentType"})
  List<DesignComponent> findByDesignComponentIdIn(List<Integer> designComponentIds);

  @EntityGraph(attributePaths = {"user", "componentTypeMappings",
      "componentTypeMappings.componentType"})
  List<DesignComponent> findByUser_PublicUserId(String userPublicUserId);

  List<DesignComponent> findByUser_UserEmailIn(List<String> userEmails);
}
