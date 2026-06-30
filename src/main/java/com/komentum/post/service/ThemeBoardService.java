package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.query.ThemeBoardQuery;
import com.komentum.post.mapper.PostDtoMapper;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.post.repository.ThemeBoardRepositorySupport;
import com.komentum.post.service.condition.PostSearchCondition;
import com.komentum.post.service.enums.PostSortType;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeBoardService {

  private final ThemeBoardRepository themeBoardRepository;
  private final ThemeBoardRepositorySupport themeBoardRepositorySupport;
  private final PostService postService;
  private final PostDtoMapper postDtoMapper;

  /**
   * <p>테마 게시글 페이징을 위한 중간 DTO 조회 ( DTO Projection )</p>
   * <p>데이터를 생성일 기준 내림차순 정렬하여 반환함</p>
   * @param pageable 페이징 정보
   * @return ThemeBoardQuery.Preview 테마 게시글 페이징 DTO 생성을 위한 중간 계층 DTO 반환
   * */
  public List<ThemeBoardQuery.Preview> findThemeBoardQueryPreview(Pageable pageable) {
    return findThemeBoardQueryPreview(pageable, null);
  }

  public List<ThemeBoardQuery.Preview> findThemeBoardQueryPreview(Pageable pageable,
      String keyword) {
    PostSearchCondition condition = new PostSearchCondition()
        .withKeyword(keyword);
    return themeBoardRepositorySupport.findThemeBoardQueryPreviewList(pageable, condition,
        List.of(PostSortType.DEFAULT));
  }

  /**
   * <p>테마 게시글 페이징을 위한 중간 DTO 조회 ( DTO Projection )</p>
   * <p>테마 게시글 DTO를 좋아요 수 순으로 정렬하여 반환함</p>
   * @param pageable 페이징 정보
   * @return desc=true이면 좋아요 순 내림차순, 아니면 오름차순 정렬하여 ThemeBoardQuery.Preview 목록 반환
   * */
  public List<ThemeBoardQuery.Preview> findThemeBoardQueryPreviewOrderByPrefers(Pageable pageable,
      Boolean desc) {
    PostSortType sortType = Boolean.TRUE.equals(desc) ?
        PostSortType.PREFER_DESC :
        PostSortType.PREFER_ASC;
    return themeBoardRepositorySupport.findThemeBoardQueryPreviewList(pageable, List.of(sortType));
  }

  /**
   * 조건에 따라 게시글 상세 정보를 조회한다
   * @param pageable 조회할 페이지 정보
   * @param client 조회에 사용할 사용자 정보
   * @param pinnedPost pageNumber=0일 때, 결과 맨 앞에 포함할 게시글 정보
   * */
  public List<ThemeBoardQuery.Detail> findThemeBoardQueryDetailList(Pageable pageable, User client,
      Post pinnedPost) {
    PostSearchCondition condition = new PostSearchCondition();
    if (pinnedPost != null) {
      // post에 대한 condition 추가
      condition = condition
          .withPinnedPostIds(List.of(pinnedPost.getPostId()))
          .withAuthorPublicId(pinnedPost.getUser().getPublicUserId());
    }
    return new ArrayList<>(
        themeBoardRepositorySupport.findThemeBoardQueryDetails(
            pageable,
            client,
            condition,
            List.of(PostSortType.DEFAULT)
        )
    );
  }

  /**
   * ThemeBoard Entity를 PostId를 기반으로 조회함
   * @param postId 조회할 ThemeBoard의 Post ID
   * @return 대상이 되는 ThemeBoard
   * */
  @Transactional(readOnly = true)
  public ThemeBoard findByPostId(Long postId) {
    return themeBoardRepository.findByPost_PostId(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(ThemeBoard.class, postId));
  }

  /**
   * ThemeBoard 정보 저장
   * @param post 생성할 ThemeBoard의 Post
   * @param themeComponent 생성할 ThemeBoard의 ThemeComponent
   * @return 생성된 ThemeBoard 반환
   * */
  @Transactional
  public ThemeBoard save(Post post, ThemeComponent themeComponent) {
    return themeBoardRepository.save(ThemeBoard.builder()
        .post(post)
        .themeComponent(themeComponent)
        .build());
  }

  /**
   * ThemeBoard 정보 삭제
   * @param postId 삭제할 ThemeBoard의 post ID
   * */
  @Transactional
  public void deleteByPostId(Long postId) {
    themeBoardRepository.deleteByPost_PostId(postId);
  }
}
