package com.komentum.post.facade;

import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.dto.query.PostQuery;
import com.komentum.post.mapper.PostMapperSupport;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostManagementFacade {

  private final UserEntityFinder userEntityFinder;
  private final PostService postService;
  private final BoardManagementHelper boardManagementHelper;
  private final PostMapperSupport postMapperSupport;

  /**
   * 특정 사용자가 카테고리에 저장한 게시글 목록 반환
   * @param clientId 카테고리에 게시글을 저장한 사용자 식별자
   * @param postType 조회할 게시글 종류 (null 이면 전체)
   * @return UserPostListResponseDto 목록
   */
  @Transactional
  public List<UserPostListResponseDto> findUserSavedPostsByCategory(String clientId,
      PostType postType, Pageable pageable) {
    User client = userEntityFinder.findUserEntity(clientId);
    List<PostQuery.UserPostListRow> savedPosts = postService.findUserSavedPosts(client, postType,
        pageable);
    return postMapperSupport.toUserPostListResponseDtoList(savedPosts);
  }

  @Transactional
  public List<UserPostListResponseDto> findUserPreferredPosts(String clientId, PostType postType,
      Pageable pageable) {
    User client = userEntityFinder.findUserEntity(clientId);
    List<PostQuery.UserPostListRow> preferredPosts = postService.findUserPreferredPosts(client,
        postType, pageable);
    return postMapperSupport.toUserPostListResponseDtoList(preferredPosts);
  }

  @Transactional
  public List<UserPostListResponseDto> findMyPostsByUser(String clientId, PostType postType,
      Pageable pageable) {
    User client = userEntityFinder.findUserEntity(clientId);
    List<PostQuery.UserPostListRow> myPosts = postService.findUserPostList(client, postType,
        pageable);
    return postMapperSupport.toUserPostListResponseDtoList(myPosts);
  }
}
