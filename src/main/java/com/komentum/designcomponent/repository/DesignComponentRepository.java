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
import org.springframework.stereotype.Repository;

@Repository
public interface DesignComponentRepository extends JpaRepository<DesignComponent, Integer> {

  @EntityGraph(attributePaths = "user")
  Optional<DesignComponent> findByDesignComponentId(Integer designComponentId);

  /**
   * 활성 컴포넌트만 대상으로 ID를 페이징하고 전체 개수를 계산한다.
   */
  @Query("select dc.designComponentId from DesignComponent dc where dc.isActive = true")
  Page<Integer> findDesignComponentIdPage(Pageable pageable);

  @EntityGraph(attributePaths = "user")
  List<DesignComponent> findByDesignComponentIdIn(Collection<Integer> designComponentIds);

  /**
   * 외부 사용자별 목록에 노출할 활성 컴포넌트와 소유자를 조회한다.
   */
  @EntityGraph(attributePaths = "user")
  List<DesignComponent> findByUser_PublicUserIdAndIsActiveTrue(String userPublicUserId);

  List<DesignComponent> findByUser_UserEmailIn(List<String> userEmails);
}
