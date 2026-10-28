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

  /**
   * 활성 컴포넌트만 대상으로 ID를 페이징하고 전체 개수를 계산한다.
   */
  @Query("select dc.designComponentId from DesignComponent dc where dc.isActive = true")
  Page<Integer> findDesignComponentIdPage(Pageable pageable);

  /**
   * 타입에 속한 활성 컴포넌트의 ID를 생성일과 ID 내림차순으로 조회한다.
   */
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

  /**
   * 외부 사용자별 목록에 노출할 활성 컴포넌트와 소유자·타입 연결을 조회한다.
   */
  @EntityGraph(attributePaths = {"user", "componentTypeMappings",
      "componentTypeMappings.componentType"})
  List<DesignComponent> findByUser_PublicUserIdAndIsActiveTrue(String userPublicUserId);

  List<DesignComponent> findByUser_UserEmailIn(List<String> userEmails);
}
