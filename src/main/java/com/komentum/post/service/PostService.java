package com.komentum.post.service;

import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.policy.PostPolicy;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final FileManager fileManager;
  private final PostPolicy postPolicy;

  /**
   * 게시글 식별자를 기반으로 게시글 조회
   * @param postId 게시글 식별자
   * @return 게시글 식별자에 대한 게시글 Entity
   * @throws EntityNotFoundException postId에 해당하는 게시글 정보가 없는 경우
   * */
  @Transactional(readOnly = true)
  public Post getPostByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("failed to find post with id : %d", postId)));
  }

  public List<Post> getPostsByUserEmail(String userEmail) {
    return postRepository.findByUser_UserEmail(userEmail);
  }

  /**
   * 게시글 생성
   * @param postCreateDto 게시글 생성 정보
   * @param author 작성자 Entity
   * @param profileImageName 프로필 이미지 이름
   * */
  @Transactional
  public Post createPost(PostCreateDto postCreateDto, User author, String profileImageName) {
    return postRepository.save(Post.createTransient(postCreateDto, author, profileImageName));
  }

  /**
   * 게시글 정보 수정
   * @param postId 게시글 ID
   * @param updateDto 게시글 갱신 정보
   * @throws AccessDeniedException 현재 사용자가 게시글 작성자가 아니고, admin 계정이 아닌 경우
   * */
  @Transactional
  public Post updatePost(Long postId, PostUpdateDto updateDto) {
    Post targetPost = getPostByPostId(postId);
    if (!postPolicy.canUpdate(targetPost.getUser())) {
      throw new AccessDeniedException("failed to update post : invalid user or role");
    }
    targetPost.update(updateDto);
    return targetPost;
  }

  /**
   * 게시글 삭제
   * @param postId 삭제할 게시글 ID
   * @throws AccessDeniedException 현재 사용자가 게시글 작성자가 아니고, admin 계정이 아닌 경우
   * */
  @Transactional
  public void deletePost(Long postId) {
    Post targetPost = getPostByPostId(postId);
    if (!postPolicy.canDelete(targetPost.getUser())) {
      throw new AccessDeniedException("failed to delete post : invalid user or role");
    }
    postRepository.deleteById(targetPost.getPostId());
  }

  // 유저가 작성한 게시글 목록 조회 메서드
  public List<UserPostListResponseDto> findUserPostList(String userEmail) {

    // 필요한 정보 추출
    return getPostsByUserEmail(userEmail).stream() // userEmail 로 Post 리스트 가져옴
        .map(post -> UserPostListResponseDto.builder().postId(post.getPostId())
            .previewImageUrl(fileManager.resolveFilePath(post.getPreviewImageName())) // url 가져오기
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .build())
        .toList(); // 리스트 변환
  }

  // 업로드 수 count 반환 메서드
  public int countPost(String publicUserId){
    return postRepository.countByUser_PublicUserId(publicUserId);
  }
}
