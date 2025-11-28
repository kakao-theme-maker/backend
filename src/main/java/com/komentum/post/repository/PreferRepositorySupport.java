package com.komentum.post.repository;

import com.komentum.post.domain.QPost;
import com.komentum.post.domain.QPrefer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PreferRepositorySupport {

  private final JPAQueryFactory queryFactory;

  public Map<Long, Long> findPreferMapByPostIds(List<Long> postIds) {
    QPost post = QPost.post;
    QPrefer prefer = QPrefer.prefer;
    return queryFactory.select(post.postId, prefer.countDistinct())
        .from(prefer)
        .where(prefer.post.postId.in(postIds))
        .groupBy(prefer.post.postId)
        .fetch()
        .stream()
        .collect(Collectors.toMap(
            t -> t.get(prefer.post.postId),
            t -> {
              Long count = t.get(prefer.countDistinct());
              return count == null ? 0 : count;
            }
        ));
  }
}
