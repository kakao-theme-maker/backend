package com.komentum.post.repository;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.QPost;
import com.komentum.post.dto.PostSummary;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.user.domain.QUser;
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

  public List<PostSummary> findPostSummaries(Pageable pageable) {
    QPost post = QPost.post;
    QUser author = QUser.user;
    List<Post> targetPosts = queryFactory.selectFrom(post)
        .leftJoin(post.user, author).fetchJoin()
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
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
}
