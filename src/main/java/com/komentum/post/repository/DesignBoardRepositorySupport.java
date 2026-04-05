package com.komentum.post.repository;

import com.komentum.post.domain.QComment;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.dto.query.QDesignBoardQuery_Detail;
import com.komentum.post.dto.query.QDesignBoardQuery_Preview;
import com.komentum.theme.component.domain.QDesignComponent;
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.dsl.NumberExpression;
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

  public DesignBoardQuery.Detail findDetailByPostId(Long postId, User client) {
    QPost post = QPost.post;
    QDesignBoard designBoard = QDesignBoard.designBoard;
    QPrefer prefer = QPrefer.prefer;
    QComment comment = QComment.comment;
    QUser user = QUser.user;
    QDesignComponent designComponent = QDesignComponent.designComponent;
    NumberExpression<Long> preferCount = prefer.countDistinct();
    NumberExpression<Long> commentCount = comment.countDistinct();
    return queryFactory.select(
            new QDesignBoardQuery_Detail(
                post.postId,
                post.title,
                post.content,
                designComponent.designComponentId,
                user.userEmail,
                user.name,
                post.createdAt,
                post.previewImageName,
                preferCount,
                commentCount,
                postRepositorySupport.isLiked(post, client),
                postRepositorySupport.isBookmarked(post, client)
            )
        )
        .from(designBoard)
        .join(designBoard.designComponent, designComponent)
        .join(designBoard.post, post)
        .join(post.user, user)
        .leftJoin(prefer).on(prefer.post.eq(post))
        .leftJoin(comment).on(comment.post.eq(post))
        .where(post.postId.eq(postId))
        .fetchOne();
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
