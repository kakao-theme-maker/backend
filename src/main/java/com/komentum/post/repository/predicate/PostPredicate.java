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
   * 활성 에셋이 없는 디자인 게시글을 페이징 전에 제외하는 조회 조건을 생성한다.
   * 테마 게시글 등 다른 종류는 에셋 활성 여부와 관계없이 유지한다.
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

  /**
   * 게시글의 활성 에셋 중 요청 타입을 가진 에셋이 있는지 판정하는 조건을 생성한다.
   * 비활성 에셋에 남은 타입 연결은 검색 일치로 취급하지 않으며, 타입이 없으면 조건을 생략한다.
   */
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
