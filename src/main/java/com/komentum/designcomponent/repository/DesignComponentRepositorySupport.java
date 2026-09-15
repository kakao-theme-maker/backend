package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.domain.QComponentType;
import com.komentum.designcomponent.domain.QDesignComponent;
import com.komentum.designcomponent.domain.QDesignComponentComponentType;
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
    QDesignComponentComponentType designComponentComponentType =
        QDesignComponentComponentType.designComponentComponentType;
    QComponentType componentType = QComponentType.componentType;

    return queryFactory
        .selectFrom(designComponent).distinct()
        .leftJoin(designComponent.componentTypeMappings, designComponentComponentType).fetchJoin()
        .leftJoin(designComponentComponentType.componentType, componentType).fetchJoin()
        .where(designComponent.user.eq(client))
        .orderBy(designComponent.createdAt.desc())
        .fetch();
  }
}
