package com.komentum.post.repository.order;

import com.komentum.post.domain.QPost;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import java.util.ArrayList;
import java.util.List;

public class PostOrder {

  public static void addPinnedOrder(QPost post, List<OrderSpecifier<?>> orders,
      List<Long> pinnedPostIds) {
    if (pinnedPostIds == null) {
      return;
    }
    NumberExpression<Integer> priority = new CaseBuilder()
        .when(post.postId.in(pinnedPostIds)).then(0)
        .otherwise(1);
    orders.add(priority.asc());
  }

  public static void addSearchMatchOrder(List<OrderSpecifier<?>> orders,
      BooleanExpression searchMatched) {
    if (searchMatched == null) {
      return;
    }
    NumberExpression<Integer> priority = new CaseBuilder()
        .when(searchMatched).then(0)
        .otherwise(1);
    orders.add(priority.asc());
  }

  public static void addKeywordMatchOrder(QPost post, List<OrderSpecifier<?>> orders,
      String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return;
    }
    String trimmedKeyword = keyword.trim();
    NumberExpression<Integer> priority = new CaseBuilder()
        .when(post.title.containsIgnoreCase(trimmedKeyword)).then(0)
        .when(post.content.containsIgnoreCase(trimmedKeyword)).then(1)
        .otherwise(2);
    orders.add(priority.asc());
  }

  public static void addOrderSpecifiers(
      List<PostSortType> sortTypeList,
      QPost post,
      List<OrderSpecifier<?>> orderSpecifiers,
      NumberExpression<Long> preferCount) {

    sortTypeList.forEach(sortType -> {
      switch (sortType) {
        case CREATED_ASC -> orderSpecifiers.add(post.createdAt.asc());
        case CREATED_DESC -> orderSpecifiers.add(post.createdAt.desc());
        case PREFER_ASC -> {
          if (preferCount == null) {
            throw new IllegalArgumentException("preferCount is null");
          }
          orderSpecifiers.add(preferCount.asc());
        }
        case PREFER_DESC -> {
          if (preferCount == null) {
            throw new IllegalArgumentException("preferCount is null");
          }
          orderSpecifiers.add(preferCount.desc());
        }
      }
    });
    if (sortTypeList.stream()
        .noneMatch(sortType -> sortType == PostSortType.CREATED_ASC
            || sortType == PostSortType.CREATED_DESC)) {
      orderSpecifiers.add(post.createdAt.desc());
    }
    orderSpecifiers.add(post.postId.desc());
  }

  public static OrderSpecifier<?>[] create(
      PostSearchCondition condition,
      List<PostSortType> sortTypes,
      QPost post,
      NumberExpression<Long> preferCount
  ) {
    return create(condition, sortTypes, post, preferCount, null);
  }

  public static OrderSpecifier<?>[] create(
      PostSearchCondition condition,
      List<PostSortType> sortTypes,
      QPost post,
      NumberExpression<Long> preferCount,
      BooleanExpression searchMatched
  ) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    addPinnedOrder(post, orderSpecifiers, condition.getPinnedPostIds());
    addSearchMatchOrder(orderSpecifiers, searchMatched);
    addKeywordMatchOrder(post, orderSpecifiers, condition.getKeyword());
    if (sortTypes != null && !sortTypes.isEmpty()) {
      addOrderSpecifiers(sortTypes, post, orderSpecifiers, preferCount);
    }
    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}
