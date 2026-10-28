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

  /**
   * 디자인 에셋은 활성 항목만, 테마는 참조 에셋의 활성 여부와 관계없이 조회하여 합친다.
   * UNION ALL 이후 통합 결과에 정렬과 페이징을 적용해 두 유형이 하나의 페이지를 공유한다.
   *
   * @throws IllegalStateException 조회 행의 ID, 유형 또는 생성일이 없는 경우
   * @throws IllegalArgumentException 알 수 없는 컴포넌트 유형이 조회된 경우
   */
  @Override
  public List<ComponentSummary> findComponentSummaryByClient(Pageable pageable, User client) {
    String query = """
        SELECT id, type, preview_image_file_name, created_at
        FROM (
          SELECT
            tc.theme_component_id as id,
            'THEME' as type,
            NULL as preview_image_file_name,
            tc.created_at as created_at
          FROM theme_component tc
          WHERE tc.user_id = :userId
          UNION ALL
          SELECT
            dc.design_component_id as id,
            'DESIGN' as type,
            dc.file_name as preview_image_file_name,
            dc.created_at as created_at
          FROM design_component dc
          WHERE dc.user_id = :userId AND dc.is_active = true
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
          // 3. previewImageFileName (nullable)
          String previewImageFileName = (String) row[2];
          // 4. createdAt (not null)
          if (row[3] == null) {
            throw new IllegalStateException(
                "ComponentSummary mapping failed: createdAt(row[3]) is null");
          }
          LocalDateTime createdAt = ((Timestamp) row[3]).toLocalDateTime();
          return new ComponentSummary(id, type, previewImageFileName, createdAt);
        })
        .toList();
    return summaries;
  }
}
