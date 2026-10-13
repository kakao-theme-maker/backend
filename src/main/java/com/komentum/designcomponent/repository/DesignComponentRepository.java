package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.DesignComponent;
import java.util.Collection;
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

  @Query("select dc.designComponentId from DesignComponent dc where dc.isActive = true")
  Page<Integer> findDesignComponentIdPage(Pageable pageable);

  @Query("""
      select dc.designComponentId
      from DesignComponent dc
      join dc.componentTypeMappings componentTypeMapping
      where componentTypeMapping.componentType.componentTypeId = :componentTypeId
      and dc.isActive = true
      order by dc.createdAt desc, dc.designComponentId desc
      """)
  List<Integer> findDesignComponentIdsByComponentTypeId(
      @Param("componentTypeId") Integer componentTypeId);

  @EntityGraph(attributePaths = {"user", "componentTypeMappings",
      "componentTypeMappings.componentType"})
  List<DesignComponent> findByDesignComponentIdIn(Collection<Integer> designComponentIds);

  @EntityGraph(attributePaths = {"user", "componentTypeMappings",
      "componentTypeMappings.componentType"})
  List<DesignComponent> findByUser_PublicUserIdAndIsActiveTrue(String userPublicUserId);

  List<DesignComponent> findByUser_UserEmailIn(List<String> userEmails);
}
