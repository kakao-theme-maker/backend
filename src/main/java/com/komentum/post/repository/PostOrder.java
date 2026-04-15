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

  public static OrderSpecifier<?>[] create(
      PostSearchCondition condition,
      List<PostSortType> sortTypes,
      QPost post,
      NumberExpression<Long> preferCount
  ) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    addPinnedOrder(post, orderSpecifiers, condition.getPinnedPostIds());
    if (sortTypes != null && !sortTypes.isEmpty()) {
      for (PostSortType sortType : sortTypes) {
        switch (sortType) {
          case DEFAULT -> orderSpecifiers.add(post.createdAt.desc());
          case PREFER_ASC -> orderSpecifiers.add(preferCount.asc());
          case PREFER_DESC -> orderSpecifiers.add(preferCount.desc());
        }
      }
    }
    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}
