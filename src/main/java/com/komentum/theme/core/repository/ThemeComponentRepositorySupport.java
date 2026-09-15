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
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ThemeComponentRepositorySupport {

  private final JPAQueryFactory queryFactory;
  private final PostRepositorySupport postRepositorySupport;

  /**
   * ThemeSearchCondition을 기반으로 동적 쿼리를 생성한다
   * @param client 요청을 보낸 client ( 인증이 불필요한 요청이면 null 가능 )
   * @param condition TestSearchCondition 객체
   * @param sortTypes 우선순위에 따라 정렬된 정렬기준
   * */
  public List<ThemeComponent> findAllThemesByCondition(
      Pageable pageable,
      User client,
      ThemeSearchCondition condition,
      List<ThemeSortType> sortTypes
  ) {
    QUser user = QUser.user;
    QThemeComponent themeComponent = QThemeComponent.themeComponent;
    QThemeBoard themeBoard = QThemeBoard.themeBoard;
    QPost post = QPost.post;
    QPrefer prefer = QPrefer.prefer;
    NumberExpression<Long> preferCount = postRepositorySupport.makePreferCountExpression(
        post,
        prefer
    );
    JPAQuery<ThemeComponent> query = queryFactory.select(themeComponent)
        .from(themeComponent);
    List<BooleanExpression> searchConditions = new ArrayList<>(
        List.of(themeComponent.isPublic.isTrue()));
    boolean withPost = false, withUser = false;
    if (sortTypes.contains(ThemeSortType.PREFER_DESC)) {
      withPost = true;
    }
    // make boolean expression with public user id
    if (condition.getPublicUserId() != null) {
      withUser = true;
      searchConditions.add(user.publicUserId.eq(condition.getPublicUserId()));
    }
    if (withPost) {
      query
          .leftJoin(themeBoard).on(themeBoard.themeComponent.eq(themeComponent))
          .join(themeBoard.post, post);
    }
    if (withUser) {
      query.join(user).on(themeComponent.userEmail.eq(user.userEmail));
    }
    // make query
    return query
        .where(searchConditions.toArray(BooleanExpression[]::new))
        .groupBy(themeComponent.themeComponentId)
        .orderBy(ThemeOrder.toOrders(sortTypes, themeComponent, preferCount))
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset())
        .fetch();
  }
}
