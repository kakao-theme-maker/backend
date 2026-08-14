package com.komentum.post.service;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.DesignBoardRepositorySupport;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesignBoardService {

  private final DesignBoardRepository designBoardRepository;
  private final DesignBoardRepositorySupport designBoardRepositorySupport;
  private final JPAQueryFactory queryFactory;

  @Transactional(readOnly = true)
  public List<DesignBoard> findAllByPostIds(List<Long> postIds) {
    QDesignBoard designBoard = QDesignBoard.designBoard;
    return queryFactory.selectFrom(designBoard)
        .where(designBoard.post.postId.in(postIds))
        .fetch();
  }

  @Transactional(readOnly = true)
  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable) {
    return findPreviewList(pageable, null, null, PostSortType.CREATED_DESC);
  }

  @Transactional(readOnly = true)
  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable, String keyword,
      TypeCode typeCode) {
    return findPreviewList(pageable, keyword, typeCode, PostSortType.CREATED_DESC);
  }

  @Transactional(readOnly = true)
  public List<DesignBoardQuery.Preview> findPreviewList(Pageable pageable, String keyword,
      TypeCode typeCode, PostSortType sortType) {
    PostSearchCondition condition = new PostSearchCondition()
        .withKeyword(keyword)
        .withTypeCode(typeCode);
    return designBoardRepositorySupport.findPreviewList(pageable, condition,
        List.of(sortType));
  }

  @Transactional(readOnly = true)
  public List<DesignBoard> findWithDesignBoardByPostId(long postId) {
    return designBoardRepository.findByPost_PostId(postId);
  }

  @Transactional(readOnly = true)
  public List<DesignBoard> findWithDesignComponentsByPostIdIn(List<Long> postIds) {
    return designBoardRepository.findWithDesignComponentByPost_PostIdIn(postIds);
  }

  @Transactional(readOnly = true)
  public Map<Long, List<DesignBoard>> findWithDesignComponentsByPostIdMap(List<Long> postIds) {
    if (postIds == null || postIds.isEmpty()) {
      return Map.of();
    }
    return findWithDesignComponentsByPostIdIn(postIds)
        .stream()
        .collect(Collectors.groupingBy(designBoard -> designBoard.getPost().getPostId()));
  }

  @Transactional(readOnly = true)
  public List<DesignBoard> findWithDesignComponentsByPostId(Long postId) {
    return findWithDesignComponentsByPostIdIn(List.of(postId));
  }

  @Transactional(readOnly = true)
  public List<DesignBoardQuery.Detail> findDetailList(Pageable pageable, User client,
      Post pinnedPost) {
    PostSearchCondition condition = new PostSearchCondition();
    if (pinnedPost != null) {
      condition = condition
          .withPinnedPostIds(List.of(pinnedPost.getPostId()))
          .withAuthorPublicId(pinnedPost.getUser().getPublicUserId());
    }
    return new ArrayList<>(
        designBoardRepositorySupport.findDesignBoardDetails(
            pageable,
            client,
            condition,
            List.of(PostSortType.CREATED_DESC)
        )
    );
  }

  @Transactional
  public DesignBoard save(Post post, DesignComponent designComponent) {
    return designBoardRepository.save(DesignBoard.builder()
        .post(post)
        .designComponent(designComponent)
        .build());
  }

  @Transactional
  public void synchronizeDesignBoards(
      Post post,
      List<DesignComponent> requestedComponents
  ) {
    // delete all design boards
    designBoardRepository.deleteByPost_PostId(post.getPostId());
    // generate all design boards
    List<DesignBoard> designBoards = findWithDesignBoardByPostId(post.getPostId());
    for (DesignComponent component : requestedComponents) {
      save(post, component);
    }
    List<DesignBoard> updated = findWithDesignBoardByPostId(post.getPostId());
    int a = 1;
  }

  @Transactional
  public void deleteByPostId(Long postId) {
    designBoardRepository.deleteByPost_PostId(postId);
  }
}
