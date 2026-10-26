package com.komentum.designcomponent.repository;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.domain.QComponentType;
import com.komentum.designcomponent.domain.QDesignComponent;
import com.komentum.designcomponent.domain.QDesignComponentComponentType;
import com.komentum.post.domain.QCategory;
import com.komentum.post.domain.QCategoryPost;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.user.domain.User;
import com.komentum.user.domain.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class DesignComponentRepositorySupport {

  private final JPAQueryFactory queryFactory;

  /**
   * 사용자가 업로드한 활성 컴포넌트를 타입 정보와 함께 생성일 내림차순으로 조회한다.
   */
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
        .where(designComponent.user.eq(client), designComponent.isActive.isTrue())
        .orderBy(designComponent.createdAt.desc())
        .fetch();
  }

  /**
   * 사용자의 북마크 게시글에 연결된 활성 컴포넌트를 생성일 내림차순으로 조회한다.
   * 여러 게시글에 연결된 동일 컴포넌트는 한 번만 반환하며 기존 연결은 변경하지 않는다.
   */
  @Transactional(readOnly = true)
  public List<DesignComponent> findBookmarkedDesignComponents(String publicUserId) {
    QDesignBoard designBoard = QDesignBoard.designBoard;
    QPost post = QPost.post;
    QUser user = QUser.user;
    QDesignComponent designComponent = QDesignComponent.designComponent;
    QCategoryPost categoryPost = QCategoryPost.categoryPost;
    QCategory category = QCategory.category;
    return queryFactory
        .select(designComponent).distinct()
        .from(categoryPost)
        .join(categoryPost.post, post)
        .join(categoryPost.category, category)
        .join(designBoard).on(designBoard.post.eq(post))
        .join(designBoard.designComponent, designComponent)
        .join(designComponent.user, user).fetchJoin()
        .where(
            category.owner.publicUserId.eq(publicUserId),
            categoryPost.category.categoryType.eq(CategoryType.BOOKMARK),
            designComponent.isActive.isTrue()
        )
        .orderBy(designComponent.createdAt.desc())
        .fetch();
  }
}
