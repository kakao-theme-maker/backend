package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.domain.QDesignComponent;
import com.komentum.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DesignComponentRepositorySupport {

  private final JPAQueryFactory queryFactory;

  @Transactional(readOnly = true)
  public List<DesignComponent> findUploadedDesignComponents(User client) {
    QDesignComponent designComponent = QDesignComponent.designComponent;

    return queryFactory
        .select(designComponent).distinct()
        .from(designComponent)
        .where(designComponent.user.eq(client))
        .orderBy(designComponent.createdAt.desc())
        .fetch();
  }
}
