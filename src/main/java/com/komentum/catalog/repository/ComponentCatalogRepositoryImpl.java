package com.komentum.catalog.repository;

import com.komentum.catalog.dto.ComponentSummary;
import com.komentum.catalog.dto.ComponentType;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
  public List<ComponentSummary> findComponentSummaryByClient(Pageable pageable, User client,
      String clientEmail) {
    String query = """
        SELECT id, type, preview_image_url, created_at
        FROM (
          SELECT
            tc.theme_component_id as id,
            'THEME' as type,
            NULL as preview_image_url,
            tc.created_at as created_at
          FROM theme_component tc
          WHERE tc.user_email = :clientEmail
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
    // execute query and retrieve results
    List<Object[]> rows = em.createNativeQuery(query)
        .setParameter("limit", pageable.getPageSize())
        .setParameter("offset", pageable.getOffset())
        .setParameter("userId", client.getUserId())
        .setParameter("clientEmail", clientEmail)
        .getResultList();
    // convert to ComponentSummary
    List<ComponentSummary> summaries = rows.stream()
        .map(row -> {
          // id (not null)
          if (row[0] == null) {
            throw new IllegalStateException(
                "ComponentSummary mapping failed: id(row[0]) is null");
          }
          int id = ((Number) row[0]).intValue();
          // 2. type (not null)
          if (row[1] == null) {
            throw new IllegalStateException(
                "ComponentSummary mapping failed: type(row[1]) is null");
          }
          ComponentType type = ComponentType.fromString((String) row[1]);
          if (type == null) {
            throw new IllegalArgumentException(
                "ComponentSummary mapping failed: Unknown ComponentType: " + row[1]);
          }
          // 3. previewImageUrl (nullable)
          String previewImageUrl = (String) row[2];
          // 4. createdAt (not null)
          if (row[3] == null) {
            throw new IllegalStateException(
                "ComponentSummary mapping failed: createdAt(row[3]) is null");
          }
          LocalDateTime createdAt = ((Timestamp) row[3]).toLocalDateTime();
          return new ComponentSummary(id, type, previewImageUrl, createdAt);
        })
        .toList();
    return summaries;
  }
}
