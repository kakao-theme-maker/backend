package com.komentum.catalog.repository;

import com.komentum.catalog.dto.ComponentSummary;
import com.komentum.catalog.dto.ComponentType;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ComponentCatalogRepositoryImpl implements ComponentCatalogRepository {

  @PersistenceContext
  private final EntityManager em;

  @Override
  public List<ComponentSummary> findComponentSummaryByClient(Pageable pageable, User client) {
    String query = """
        SELECT id, type, preview_image_url, created_at
        FROM (
          SELECT
            tc.theme_component_id as id,
            'THEME' as type,
            NULL as preview_image_url,
            tc.created_at as created_at
          FROM theme_component tc
          JOIN "user" u ON tc.user_email = u.user_email
          WHERE u.user_id = :userId
          UNION ALL
          SELECT
            dc.design_component_id as id,
            'DESIGN' as type,
            dc.image_url as preview_image_url,
            dc.created_at as created_at
          FROM design_component dc
          WHERE dc.user_id = :userId
        ) combined
        ORDER BY created_at DESC
        LIMIT :limit
        OFFSET :offset
        """;

    List<Object[]> rows = em.createNativeQuery(query)
        .setParameter("limit", pageable.getPageSize())
        .setParameter("offset", pageable.getOffset())
        .setParameter("userId", client.getUserId())
        .getResultList();
    List<ComponentSummary> summaries = rows.stream()
        .map(row -> new ComponentSummary(
            ((Number) row[0]).intValue(),
            ComponentType.fromString((String) row[1]),
            (String) row[2],
            ((Timestamp) row[3]).toLocalDateTime()
        ))
        .toList();
    return summaries;
  }
}
