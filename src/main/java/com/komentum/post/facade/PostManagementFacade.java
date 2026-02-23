package com.komentum.post.facade;

import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.service.PostService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostManagementFacade {

  private final UserEntityFinder userEntityFinder;
  private final PostService postService;
  private final BoardManagementHelper boardManagementHelper;

  /**
   * 특정 사용자가 카테고리에 저장한 게시글 목록 반환
   * @param clientId 카테고리에 게시글을 저장한 사용자 식별자
   * @return UserPostListResponseDto 목록
   */
  @Transactional
  public List<UserPostListResponseDto> findUserSavedPostsByCategory(String clientId) {
    User client = userEntityFinder.findUserEntity(clientId);
    return postService.findUserSavedPosts(client).stream()
        .map(p -> UserPostListResponseDto.from(p, boardManagementHelper))
        .toList();
  }
}
