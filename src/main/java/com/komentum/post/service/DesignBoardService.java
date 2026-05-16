package com.komentum.post.service;

import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.QDesignBoard;
import com.komentum.post.dto.query.DesignBoardQuery;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.DesignBoardRepositorySupport;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    return designBoardRepositorySupport.findPreviewList(pageable);
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
            List.of(PostSortType.DEFAULT)
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
      List<DesignComponent> currentComponents,
      List<DesignComponent> requestedComponents
  ) {
    // generate current / requested design component id set
    Set<Integer> requestedDCIDSet = requestedComponents.stream()
        .map(DesignComponent::getDesignComponentId)
        .collect(Collectors.toSet());
    Set<Integer> currentDCIDSet = currentComponents.stream()
        .map(DesignComponent::getDesignComponentId)
        .collect(Collectors.toSet());
    // delete design boards from current design component list
    List<DesignComponent> componentsToDelete = currentComponents.stream()
        .filter(dc -> !requestedDCIDSet.contains(dc.getDesignComponentId()))
        .toList();
    designBoardRepository.deleteByPostAndDesignComponentIn(post, componentsToDelete);
    // generate design boards from requested design component list
    List<DesignComponent> componentsToGenerate = requestedComponents.stream()
        .filter(dc -> !currentDCIDSet.contains(dc.getDesignComponentId()))
        .toList();
    for (DesignComponent dc : componentsToGenerate) {
      save(post, dc);
    }
  }

  @Transactional
  public void deleteByPostId(Long postId) {
    designBoardRepository.deleteByPost_PostId(postId);
  }
}