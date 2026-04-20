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
}
