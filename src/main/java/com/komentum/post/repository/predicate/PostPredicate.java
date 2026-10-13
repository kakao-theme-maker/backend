package com.komentum.post.repository.predicate;

import com.komentum.designcomponent.domain.QDesignComponentComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.enums.PostType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

public class PostPredicate {

  /**
   * 활성 에셋이 없는 디자인 게시글만 제외하는 조회 조건을 생성한다. 다른 게시글 종류는 유지한다.
   */
  public static BooleanExpression visibleDesignPost(QPost post) {
    QDesignBoard visibleDesignBoard = new QDesignBoard("visibleDesignBoard");
    return post.postType.ne(PostType.DESIGN_BOARD).or(
        JPAExpressions.selectOne()
            .from(visibleDesignBoard)
            .where(visibleDesignBoard.post.eq(post),
                visibleDesignBoard.designComponent.isActive.isTrue())
            .exists());
  }

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
            searchDesignBoard.designComponent.isActive.isTrue(),
            componentTypeMapping.componentType.typeCode.eq(typeCode)
        )
        .exists();
  }
}
