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

  @Query("select dc.designComponentId from DesignComponent dc")
  Page<Integer> findDesignComponentIdPage(Pageable pageable);

  @EntityGraph(attributePaths = "user")
  List<DesignComponent> findByDesignComponentIdIn(Collection<Integer> designComponentIds);

  @EntityGraph(attributePaths = "user")
  List<DesignComponent> findByUser_PublicUserId(String userPublicUserId);

  List<DesignComponent> findByUser_UserEmailIn(List<String> userEmails);
}
