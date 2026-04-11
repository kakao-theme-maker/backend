package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.QCategory;
import com.komentum.post.domain.QCategoryPost;
import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.komentum.post.dto.PostSummary;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.user.domain.QUser;
import com.komentum.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
   * DB에서 사용자가 카테고리에 저장한 게시글 목록 조회
   * @param user 카테고리에 게시글을 저장한 User 엔티티
   * @return 카테고리에 저장된 게시글 목록
   * */
  public List<Post> findUserSavedPost(User user) {
    QPost post = QPost.post;
    QCategory category = QCategory.category;
    QCategoryPost categoryPost = QCategoryPost.categoryPost;
    return queryFactory.select(post)
        .from(categoryPost)
        .join(categoryPost.post, post)
        .join(categoryPost.category, category)
        .where(category.owner.eq(user))
        .fetch();
  }

  public BooleanExpression isLiked(QPost post, User user) {
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

  public BooleanExpression isBookmarked(QPost post, User user) {
    QCategoryPost categoryPost = QCategoryPost.categoryPost;

    return JPAExpressions
        .selectOne()
        .from(categoryPost)
        .where(
            categoryPost.post.eq(post),
            categoryPost.category.owner.eq(user)
        )
        .exists();
  }

  /**
   * 사용자가 좋아요를 누른 게시글 목록 조회
   * */
  public List<Post> findUserPreferredPosts(User client) {
    QPost post = QPost.post;
    QPrefer prefer = QPrefer.prefer;
    QUser user = QUser.user;
    return queryFactory.select(post)
        .from(prefer)
        .join(prefer.post, post)
        .where(prefer.user.eq(client))
        .fetch();
  }
}
