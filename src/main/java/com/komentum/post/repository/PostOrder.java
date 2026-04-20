package com.komentum.post.repository;

import com.komentum.post.domain.QPost;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
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

  public static void addOrderSpecifiers(
      List<PostSortType> sortTypeList,
      QPost post,
      List<OrderSpecifier<?>> orderSpecifiers,
      NumberExpression<Long> preferCount) {

    sortTypeList.forEach(sortType -> {
      OrderSpecifier<?> orderSpecifier = switch (sortType) {
        case DEFAULT -> post.createdAt.desc();
        case PREFER_ASC -> {
          if (preferCount == null) {
            throw new IllegalArgumentException("preferCount is null");
          }
          yield preferCount.asc();
        }
        case PREFER_DESC -> {
          if (preferCount == null) {
            throw new IllegalArgumentException("preferCount is null");
          }
          yield preferCount.desc();
        }
      };
      orderSpecifiers.add(orderSpecifier);
    });
  }

  public static OrderSpecifier<?>[] create(
      PostSearchCondition condition,
      List<PostSortType> sortTypes,
      QPost post,
      NumberExpression<Long> preferCount
  ) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    addPinnedOrder(post, orderSpecifiers, condition.getPinnedPostIds());
    if (sortTypes != null && !sortTypes.isEmpty()) {
      addOrderSpecifiers(sortTypes, post, orderSpecifiers, preferCount);
    }
    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}
