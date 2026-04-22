package com.komentum.post.repository;

import com.komentum.post.domain.QComment;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.domain.QThemeBoard;
import com.komentum.post.dto.query.QThemeBoardQuery_Detail;
import com.komentum.post.dto.query.QThemeBoardQuery_Preview;
import com.komentum.post.dto.query.ThemeBoardQuery;
import com.komentum.post.dto.query.ThemeBoardQuery.Preview;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.theme.theme.domain.QThemeComponent;
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ThemeBoardRepositorySupport {

  private final JPAQueryFactory queryFactory;
  private final PostRepositorySupport postRepositorySupport;

  /**
   * <p>DTO Projection을 활용하여 ThemeBoardQuery.Preview 목록 조회</p>
   * <b>주의 : DTO Projection이므로 영속성 컨텍스트에 Entity가 저장되지 않음</b>
   * @param pageable 페이징 정보
   * @param sortTypeList 정렬 기준 목록 ( 정렬 기준 여러개 사용 가능 )
   * @return ThemeBoardQuery.preview 목록 반환
   * */
  public List<Preview> findThemeBoardQueryPreviewList(Pageable pageable,
      List<PostSortType> sortTypeList) {
    QPost post = QPost.post;
    QThemeBoard themeBoard = QThemeBoard.themeBoard;
    QPrefer prefer = QPrefer.prefer;
    QUser user = QUser.user;
    QThemeComponent themeComponent = QThemeComponent.themeComponent;
    NumberExpression<Long> preferCount = prefer.countDistinct();
    // sort type
    List<? extends OrderSpecifier<?>> orderSpecifiers = getOrderSpecifiers(sortTypeList, post,
        preferCount);
    // generate JPQL
    return queryFactory.select(
            new QThemeBoardQuery_Preview(
                post.postId,
                themeComponent.themeComponentId,
                post.title,
                post.previewImageName,
                user.userEmail,
                post.createdAt,
                preferCount
            )
        )
        .from(themeBoard)
        .join(themeBoard.themeComponent, themeComponent)
        .join(themeBoard.post, post)
        .join(post.user, user)
        .leftJoin(prefer).on(prefer.post.eq(post))
        .groupBy(
            post.postId,
            themeBoard.themeBoardId,
            themeComponent.themeComponentId,
            user.userId
        )
        .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }

  /**
   * ThemeBoardQuery.Detail 조회를 위한 공통 부분 분리
   * */
  private JPAQuery<ThemeBoardQuery.Detail> getThemeBoardDetailBaseQuery(User client) {
    QPost post = QPost.post;
    QPrefer prefer = QPrefer.prefer;
    QComment comment = QComment.comment;
    QUser user = QUser.user;
    QThemeBoard themeBoard = QThemeBoard.themeBoard;
    QThemeComponent themeComponent = QThemeComponent.themeComponent;

    JPQLQuery<Long> preferCount =
        JPAExpressions
            .select(prefer.count())
            .from(prefer)
            .where(prefer.post.eq(post));

    JPQLQuery<Long> commentCount =
        JPAExpressions
            .select(comment.count())
            .from(comment)
            .where(comment.post.eq(post));

    return queryFactory
        .select(new QThemeBoardQuery_Detail(
            post.postId,
            post.title,
            post.content,
            themeComponent.themeComponentId,
            user.userEmail,
            user.name, //username
            post.createdAt,
            post.previewImageName,
            preferCount,
            commentCount,
            postRepositorySupport.isLiked(post, client),
            postRepositorySupport.isBookmarked(post, client),
            user.profileImgUrl
        ))
        .from(themeBoard)
        .join(themeBoard.themeComponent, themeComponent)
        .join(themeBoard.post, post)
        .join(post.user, user);
  }

  /**
   * 테마 게시글 상세 정보 단건 조회
   * */
  public ThemeBoardQuery.Detail findThemeBoardQueryDetail(Long postId, User client) {
    QPost post = QPost.post;
    return getThemeBoardDetailBaseQuery(client)
        .where(post.postId.eq(postId))
        .fetchOne();
  }

  /**
   * 테마 게시글 목록 상세 정보 일괄 조회
   * */
  public List<ThemeBoardQuery.Detail> findThemeBoardQueryDetails(Pageable pageable, User client,
      PostSearchCondition condition, List<PostSortType> sortTypes) {
    QPost post = QPost.post;
    OrderSpecifier<?>[] orderSpecifiers = PostOrder.create(condition, sortTypes, post, null);
    return getThemeBoardDetailBaseQuery(client)
        .where(
            PostPredicate.userPublicIdEq(post, condition.getAuthorPublicId())
        )
        .orderBy(orderSpecifiers)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }

  /**
   * 테마 게시글 정렬 기준 생성
   * */
  private List<? extends OrderSpecifier<?>> getOrderSpecifiers(
      List<PostSortType> sortTypeList, QPost post,
      NumberExpression<Long> preferCount) {
    return sortTypeList.stream()
        .flatMap(sortType -> switch (sortType) {
          case DEFAULT -> Stream.of(post.createdAt.desc());
          case PREFER_ASC -> Stream.of(preferCount.asc());
          case PREFER_DESC -> Stream.of(preferCount.desc());
        })
        .toList();
  }
}
