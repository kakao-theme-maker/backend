package com.komentum.theme.core.repository;

import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.domain.QThemeBoard;
import com.komentum.post.repository.PostRepositorySupport;
import com.komentum.theme.core.domain.QThemeComponent;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.enums.ThemeSortType;
import com.komentum.theme.core.repository.order.ThemeOrder;
import com.komentum.theme.core.service.condition.ThemeSearchCondition;
import com.komentum.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ThemeComponentRepositorySupport {

  private final JPAQueryFactory queryFactory;
  private final PostRepositorySupport postRepositorySupport;

  public List<ThemeComponent> findAllThemesByCondition(
      Pageable pageable,
      User client,
      ThemeSearchCondition condition,
      List<ThemeSortType> sortTypes
  ) {
    QThemeComponent themeComponent = QThemeComponent.themeComponent;
    QThemeBoard themeBoard = QThemeBoard.themeBoard;
    QPost post = QPost.post;
    QPrefer prefer = QPrefer.prefer;
    NumberExpression<Long> preferCount = postRepositorySupport.makePreferCountExpression(
        post,
        prefer
    );
    BooleanExpression isBookmarked = condition.getBookmarked() ?
        postRepositorySupport.isBookmarked(post, client) :
        null;
    return queryFactory.select(themeComponent)
        .from(themeBoard)
        .join(themeBoard.themeComponent, themeComponent)
        .join(themeBoard.post, post)
        .where(isBookmarked, themeComponent.isPublic.isTrue())
        .groupBy(themeComponent.themeComponentId)
        .orderBy(ThemeOrder.toOrders(sortTypes, themeComponent, preferCount))
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset())
        .fetch();
  }
}
