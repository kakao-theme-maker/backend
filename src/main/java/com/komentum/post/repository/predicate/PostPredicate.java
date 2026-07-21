package com.komentum.post.repository.predicate;

import com.komentum.designcomponent.domain.QDesignComponentComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

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

  public static BooleanExpression designComponentTypeCodeExists(QPost post, TypeCode typeCode) {
    if (typeCode == null) {
      return null;
    }
    QDesignBoard searchDesignBoard = new QDesignBoard("searchDesignBoard");
    QDesignComponentComponentType componentTypeMapping =
        new QDesignComponentComponentType("searchComponentTypeMapping");
    return JPAExpressions.selectOne()
        .from(searchDesignBoard)
        .join(searchDesignBoard.designComponent.componentTypeMappings, componentTypeMapping)
        .where(
            searchDesignBoard.post.eq(post),
            componentTypeMapping.componentType.typeCode.eq(typeCode)
        )
        .exists();
  }
}
