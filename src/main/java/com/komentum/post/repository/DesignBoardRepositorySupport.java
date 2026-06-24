package com.komentum.post.repository;

import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.dto.query.QDesignBoardQuery_Detail;
import com.komentum.post.dto.query.QDesignBoardQuery_Preview;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.designcomponent.domain.QDesignComponent;
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.OrderSpecifier;
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
                user.profileImgUrl
            )
        )
        .from(designBoard)
        .join(designBoard.designComponent, designComponent)
        .join(designBoard.post, post)
        .join(post.user, user);
  }

  public DesignBoardQuery.Detail findDetailByPostId(Long postId, User client) {
    QPost post = QPost.post;
    return getDesignBoardDetailBaseQuery(client)
        .where(post.postId.eq(postId))
        .groupBy(post.postId)
        .fetchOne();
  }

  public List<DesignBoardQuery.Detail> findDesignBoardDetails(
      Pageable pageable,
      User client,
      PostSearchCondition condition,
      List<PostSortType> sortTypes
  ) {
    QPost post = QPost.post;
    OrderSpecifier<?>[] orderSpecifiers = PostOrder.create(condition, sortTypes, post, null);
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

  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable) {
    QPost post = QPost.post;
    QDesignBoard designBoard = QDesignBoard.designBoard;
    QPrefer prefer = QPrefer.prefer;
    QUser user = QUser.user;
    QDesignComponent designComponent = QDesignComponent.designComponent;
    NumberExpression<Long> preferCount = prefer.countDistinct();
    return queryFactory.select(
            new QDesignBoardQuery_Preview(
                post.postId,
                designComponent.designComponentId,
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
        .groupBy(
            post.postId,
            designComponent.designComponentId,
            designBoard.designBoardId
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }
}
