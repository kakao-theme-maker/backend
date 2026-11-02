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
import java.util.Comparator;
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

  /**
   * 게시글의 전체 연결을 에셋·타입과 함께 조회한다.
   * 대표 이미지의 대체 여부와 기존 연결 보존을 판단해야 하므로 비활성 에셋도 포함한다.
   */
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

  /**
   * 비활성 에셋이 포함된 게시글의 대표 이미지를 활성 에셋 중 ID가 가장 작은 에셋의 파일명으로 대체한다.
   * 기존 합성 이미지에 비활성 에셋이 남지 않도록 대체하며, 최소 ID로 조회 순서와 무관하게 선택한다.
   *
   * @param designBoards 비활성 에셋을 포함한 게시글의 전체 연결 목록
   * @return 모두 활성이거나 목록이 비어 있으면 기존 파일명, 일부 비활성이면 가장 작은 활성 ID의 파일명,
   *     비활성만 있으면 null
   */
  public String resolveVisiblePreviewImageName(String previewImageName,
      List<DesignBoard> designBoards) {
    if (designBoards.stream()
        .allMatch(board -> Boolean.TRUE.equals(board.getDesignComponent().getIsActive()))) {
      return previewImageName;
    }
    return designBoards.stream()
        .map(DesignBoard::getDesignComponent)
        .filter(component -> Boolean.TRUE.equals(component.getIsActive()))
        .min(Comparator.comparing(DesignComponent::getDesignComponentId))
        .map(DesignComponent::getFileName)
        .orElse(null);
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

  /**
   * 게시글의 연결행을 요청한 에셋 목록으로 교체한다.
   * 기존 연결행만 삭제·재생성하며 에셋 엔티티나 원본 이미지는 삭제하지 않는다.
   */
  @Transactional
  public void synchronizeDesignBoards(
      Post post,
      List<DesignComponent> requestedComponents
  ) {
    // DesignBoard는 게시글과 에셋의 연결행이며 에셋 자체가 아니다.
    designBoardRepository.deleteByPost_PostId(post.getPostId());
    // 검증된 요청 목록으로 연결을 다시 생성한다.
    List<DesignBoard> designBoards = findWithDesignBoardByPostId(post.getPostId());
    for (DesignComponent component : requestedComponents) {
      save(post, component);
    }
    List<DesignBoard> updated = findWithDesignBoardByPostId(post.getPostId());
    int a = 1;
  }

  /**
   * 게시글의 에셋 연결행만 삭제하고 연결 대상 에셋과 원본 이미지는 보존한다.
   */
  @Transactional
  public void deleteByPostId(Long postId) {
    designBoardRepository.deleteByPost_PostId(postId);
  }
}
