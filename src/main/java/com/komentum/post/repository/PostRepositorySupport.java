package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.QComment;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.dto.query.PostQuery;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.user.domain.QFollow;
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositorySupport {

  private final JPAQueryFactory queryFactory;
  private final PreferRepository preferRepository;
  private final PreferRepositorySupport preferRepositorySupport;

  /**
   * post Id를 기반으로 Post의 aggregate 객체 반환 ( PostSummary )
   *
   */
  public PostSummary findPostSummaryByPostId(Long postId) {
    QPost post = QPost.post;
    QUser author = QUser.user;
    Post targetPost = queryFactory.selectFrom(post)
        .leftJoin(post.user, author).fetchJoin()
        .where(post.postId.eq(postId)).fetchOne();
    if (targetPost == null) {
      throw new ResourceNotFoundException("Post with id " + postId + " not found");
    }
    Long prefers = preferRepository.countPreferByPost_PostId(postId);
    return PostSummary.builder()
        .post(targetPost)
        .author(targetPost.getUser())
        .prefers(prefers)
        .build();
  }

  /**
   * post의 aggregate 객체 목록 반환 ( PostSummary )
   *
   */
  public List<PostSummary> findPostSummaries(Pageable pageable) {
    QPost post = QPost.post;
    QUser author = QUser.user;
    List<Post> targetPosts = queryFactory.selectFrom(post)
        .leftJoin(post.user, author).fetchJoin()
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
    return findPostSummaries(targetPosts, pageable);
  }

  /**
   * post의 aggregate 객체 목록 반환 ( PostSummary )
   * */
  public List<PostSummary> findPostSummaries(List<Post> targetPosts, Pageable pageable) {
    List<Long> postIds = targetPosts.stream().map(Post::getPostId).toList();
    Map<Long, Long> postPreferMap = preferRepositorySupport
        .findPreferMapByPostIds(postIds);
    return targetPosts.stream().map(p -> PostSummary
        .builder()
        .post(p)
        .author(p.getUser())
        .prefers(postPreferMap.get(p.getPostId()))
        .build()).toList();
  }

  /**
   * 사용자가 좋아요를 누른 게시글 목록 조회
   * */
  public List<PostQuery.UserPostListRow> findUserPreferredPosts(User client, PostType postType,
      Pageable pageable) {
    QPost post = QPost.post;
    QPrefer prefer = QPrefer.prefer;
    QUser user = QUser.user;
    return queryFactory.select(userPostListProjection(post, user, client))
        .from(prefer)
        .join(prefer.post, post)
        .join(post.user, user)
        .where(
            prefer.user.eq(client),
            postTypeEq(post, postType)
        )
        .orderBy(post.createdAt.desc(), post.postId.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }

  /**
   * 사용자가 소유한 게시글 목록 조회
   * */
  public List<PostQuery.UserPostListRow> findMyPostsByUser(User client, PostType postType,
      Pageable pageable) {
    QPost post = QPost.post;
    QUser user = QUser.user;
    return queryFactory.select(userPostListProjection(post, user, client))
        .from(post)
        .join(post.user, user)
        .where(
            post.user.eq(client),
            postTypeEq(post, postType)
        )
        .orderBy(post.createdAt.desc(), post.postId.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
  }

  private ConstructorExpression<PostQuery.UserPostListRow> userPostListProjection(QPost post,
      QUser user, User client) {
    return Projections.constructor(PostQuery.UserPostListRow.class,
        post.postId,
        post.postType,
        post.title,
        post.content,
        post.previewImageName,
        post.createdAt,
        post.updatedAt,
        user.name,
        user.profileImgUrl,
        countPrefers(post),
        countComments(post),
        isPreferred(post, client)
    );
  }

  private BooleanExpression postTypeEq(QPost post, PostType postType) {
    return postType == null ? null : post.postType.eq(postType);
  }

  /**
   * 조회 기준 사용자의 게시글 좋아요 여부를 확인하는 표현식을 생성한다.
   *
   * @param post 게시글 경로
   * @param user 조회 기준 사용자 (null 허용)
   * @return 조회 기준 사용자가 null이면 false, 아니면 좋아요 관계의 존재 여부
   */
  public BooleanExpression isPreferred(QPost post, User user) {
    if (user == null) {
      return Expressions.FALSE;
    }
    QPrefer prefer = QPrefer.prefer;
    return JPAExpressions
        .selectOne()
        .from(prefer)
        .where(
            prefer.post.eq(post),
            prefer.user.eq(user)
        )
        .exists();
  }

  /**
   * 현재 사용자가 게시글 작성자를 팔로우하는지 확인하는 표현식을 생성한다.
   *
   * @param author 게시글 작성자 경로
   * @param client 조회 기준 사용자 (null 허용)
   * @return 조회 기준 사용자가 null이면 false, 아니면 작성자 팔로우 관계의 존재 여부
   */
  public BooleanExpression isFollowing(QUser author, User client) {
    if (client == null) {
      return Expressions.FALSE;
    }
    QFollow follow = QFollow.follow;
    return JPAExpressions
        .selectOne()
        .from(follow)
        .where(
            follow.follower.eq(client),
            follow.followee.eq(author)
        )
        .exists();
  }

  public JPQLQuery<Long> countPrefers(QPost post) {
    QPrefer prefer = QPrefer.prefer;
    return JPAExpressions.select(prefer.count())
        .from(prefer)
        .where(prefer.post.eq(post));
  }

  public JPQLQuery<Long> countComments(QPost post) {
    QComment comment = QComment.comment;
    return JPAExpressions.select(comment.count())
        .from(comment)
        .where(comment.post.eq(post));
  }

  public NumberExpression<Long> makePreferCountExpression(QPost post, QPrefer prefer) {
    return Expressions.numberTemplate(
        Long.class,
        "({0})",
        JPAExpressions
            .select(prefer.preferId.countDistinct())
            .from(prefer)
            .where(prefer.post.eq(post))
    );
  }
}
