package com.komentum.theme.component.repository;

import com.komentum.post.domain.QCategory;
import com.komentum.post.domain.QCategoryPost;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.domain.QDesignComponent;
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
            categoryPost.category.categoryType.eq(CategoryType.BOOKMARK)
        )
        .orderBy(designComponent.createdAt.desc())
        .fetch();
  }
}
