package com.komentum.post.repository;

import com.komentum.post.domain.QPost;
import com.querydsl.core.types.dsl.BooleanExpression;

public class PostPredicate {

  public static BooleanExpression userPublicIdEq(QPost post, String publicUserId) {
    if (publicUserId == null) {
      return null;
    }
    return post.user.publicUserId.eq(publicUserId);
  }

  public static BooleanExpression keywordContains(QPost post, String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    String trimmedKeyword = keyword.trim();
    return post.title.containsIgnoreCase(trimmedKeyword)
        .or(post.content.containsIgnoreCase(trimmedKeyword));
  }
}
