package com.komentum.post.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.post.domain.QPost;
import com.komentum.user.domain.QUser;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PostRepositorySupportTest {

  private final PostRepositorySupport postRepositorySupport = new PostRepositorySupport(
      Mockito.mock(JPAQueryFactory.class),
      Mockito.mock(PreferRepository.class),
      Mockito.mock(PreferRepositorySupport.class)
  );

  /**
   * 조회 기준 사용자가 null이면 작성자 팔로우 여부 표현식이 false 상수로 생성되는지 검증한다.
   */
  @Test
  @DisplayName("현재 사용자가 null이면 작성자 팔로우 여부는 false 상수식을 반환한다")
  void isFollowing_nullClientReturnsFalseExpression() {
    assertThat(postRepositorySupport.isFollowing(QUser.user, null))
        .isSameAs(Expressions.FALSE);
  }

  /**
   * 조회 기준 사용자가 null이면 게시글 좋아요 여부 표현식이 false 상수로 생성되는지 검증한다.
   */
  @Test
  @DisplayName("현재 사용자가 null이면 게시글 좋아요 여부는 false 상수식을 반환한다")
  void isPreferred_nullClientReturnsFalseExpression() {
    assertThat(postRepositorySupport.isPreferred(QPost.post, null))
        .isSameAs(Expressions.FALSE);
  }

}
