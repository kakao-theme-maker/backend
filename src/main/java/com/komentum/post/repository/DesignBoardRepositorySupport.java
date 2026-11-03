package com.komentum.post.repository;

import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.dto.query.QDesignBoardQuery_Detail;
import com.komentum.post.dto.query.QDesignBoardQuery_Preview;
import com.komentum.post.repository.order.PostOrder;
import com.komentum.post.repository.predicate.PostPredicate;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.designcomponent.domain.QDesignComponent;
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DesignBoardRepositorySupport {

  private final JPAQueryFactory queryFactory;
  private final PostRepositorySupport postRepositorySupport;

  /**
   * 활성 에셋 연결만 대상으로 게시글 상세·사용자 반응 정보를 조회하는 공통 쿼리를 구성한다.
   * 활성 에셋이 없는 게시글은 집계와 페이징 전에 제외한다.
   */
  private JPAQuery<DesignBoardQuery.Detail> getDesignBoardDetailBaseQuery(User client) {
    QPost post = QPost.post;
    QDesignBoard designBoard = QDesignBoard.designBoard;
    QUser user = QUser.user;
    QDesignComponent designComponent = QDesignComponent.designComponent;
    JPQLQuery<Long> preferCount = postRepositorySupport.countPrefers(post);
    JPQLQuery<Long> commentCount = postRepositorySupport.countComments(post);

    return queryFactory.select(
            new QDesignBoardQuery_Detail(
                post.postId,
                post.title,
                post.content,
                post.previewImageName,
                user.userEmail,
                user.name,
                post.createdAt,
                preferCount,
                commentCount,
                postRepositorySupport.isPreferred(post, client),
                postRepositorySupport.isBookmarked(post, client),
                postRepositorySupport.isFollowing(user, client),
                user.profileImgUrl
            )
        )
        .from(designBoard)
        .join(designBoard.designComponent, designComponent)
        .join(designBoard.post, post)
        .join(post.user, user)
        .where(designComponent.isActive.isTrue());
  }

  /**
   * 활성 에셋이 남은 게시글을 연결 수와 무관하게 한 건으로 집계한다.
   * 게시글이 없거나 모든 에셋이 비활성이면 null을 반환한다.
   */
  public DesignBoardQuery.Detail findDetailByPostId(Long postId, User client) {
    QPost post = QPost.post;
    return getDesignBoardDetailBaseQuery(client)
        .where(post.postId.eq(postId))
        .groupBy(post.postId)
        .fetchOne();
  }

  /**
   * 활성 에셋이 있는 게시글을 게시글 ID로 묶은 뒤 상세 목록을 페이징한다.
   * 한 게시글의 활성 에셋·타입 연결 수가 페이지 행 수를 늘리지 않는다.
   */
  public List<DesignBoardQuery.Detail> findDesignBoardDetails(
      Pageable pageable,
      User client,
      PostSearchCondition condition,
      List<PostSortType> sortTypes
  ) {
    QPost post = QPost.post;
    BooleanExpression searchMatched = createSearchMatched(post, condition);
    OrderSpecifier<?>[] orderSpecifiers = PostOrder.create(condition, sortTypes, post, null,
        searchMatched);
    return getDesignBoardDetailBaseQuery(client)
        .where(
            PostPredicate.userPublicIdEq(post, condition.getAuthorPublicId())
        )
        .groupBy(post.postId)
        .orderBy(orderSpecifiers)
        .limit(pageable.getPageSize())
        .offset(pageable.getOffset())
        .fetch();
  }

  private BooleanExpression createSearchMatched(QPost post, PostSearchCondition condition) {
    BooleanExpression keywordMatched = PostPredicate.keywordContains(post, condition.getKeyword());
    BooleanExpression typeCodeMatched = PostPredicate.designComponentTypeCodeExists(post,
        condition.getTypeCode());
    if (keywordMatched == null) {
      return typeCodeMatched;
    }
    if (typeCodeMatched == null) {
      return keywordMatched;
    }
    return keywordMatched.and(typeCodeMatched);
  }

  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable) {
    return findPreviewList(pageable, new PostSearchCondition(),
        List.of(PostSortType.CREATED_DESC));
  }

  /**
   * 활성 에셋이 있는 디자인 게시글 요약을 게시글 단위로 묶어 페이지 조회한다.
   * 복수 에셋·타입 매핑에도 게시글을 한 번만 반환하고 좋아요 수를 중복 집계하지 않는다.
   * 검색어와 타입 조건은 일치하는 게시글을 우선 정렬하는 데 사용한다.
   */
  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable,
      PostSearchCondition condition, List<PostSortType> sortTypes) {
    QPost post = QPost.post;
    QDesignBoard designBoard = QDesignBoard.designBoard;
    QPrefer prefer = QPrefer.prefer;
    QUser user = QUser.user;
    QDesignComponent designComponent = QDesignComponent.designComponent;
    NumberExpression<Long> preferCount = prefer.countDistinct();
    BooleanExpression searchMatched = createSearchMatched(post, condition);
    OrderSpecifier<?>[] orderSpecifiers = PostOrder.create(condition, sortTypes, post, preferCount,
        searchMatched);
    return queryFactory.select(
            new QDesignBoardQuery_Preview(
                post.postId,
                post.title,
                post.previewImageName,
                user.userEmail,
                post.createdAt,
                preferCount
            )
        )
        .from(designBoard)
        .join(designBoard.designComponent, designComponent)
        .join(designBoard.post, post)
        .join(post.user, user)
        .leftJoin(prefer).on(prefer.post.eq(post))
        .where(
            designComponent.isActive.isTrue(),
            PostPredicate.userPublicIdEq(post, condition.getAuthorPublicId())
        )
        .groupBy(
            post.postId,
            post.title,
            post.previewImageName,
            user.userEmail,
            post.createdAt
        )
        .orderBy(orderSpecifiers)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }
}
