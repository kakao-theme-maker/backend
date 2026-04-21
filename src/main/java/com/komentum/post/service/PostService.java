package com.komentum.post.service;

import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.domain.policy.PostPolicy;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.query.PostQuery;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.PostRepositorySupport;
import com.komentum.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final PostRepositorySupport postRepositorySupport;
  private final FileManager fileManager;
  private final PostPolicy postPolicy;

  /**
   * 게시글 식별자를 기반으로 게시글 조회
   *
   * @param postId 게시글 식별자
   * @return 게시글 식별자에 대한 게시글 Entity
   * @throws EntityNotFoundException postId에 해당하는 게시글 정보가 없는 경우
   *
   */
  @Transactional(readOnly = true)
  public Post getPostByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("failed to find post with id : %d", postId)));
  }

  public List<Post> getPostsByPublicUserId(String publicUserId) {
    return postRepository.findByUser_PublicUserId(publicUserId);
  }

  public Post findByPostIdAndPostType(Long postId, PostType postType) {
    return postRepository.findByPostIdAndPostType(postId, postType)
        .orElseThrow(() -> new EntityNotFoundException(
            "cannot find " + postType.name() + " post with id : " + postId));
  }

  /**
   * 게시글 생성
   *
   * @param postCreateDto    게시글 생성 정보
   * @param author           작성자 Entity
   * @param profileImageName 프로필 이미지 이름
   *
   */
  @Transactional
  public Post createPost(PostCreateDto postCreateDto, User author, String profileImageName,
      PostType postType) {
    return postRepository.save(
        Post.createTransient(postCreateDto, author, profileImageName, postType));
  }

  /**
   * 게시글 정보 수정
   *
   * @param postId    게시글 ID
   * @param updateDto 게시글 갱신 정보
   * @throws AccessDeniedException 현재 사용자가 게시글 작성자가 아니고, admin 계정이 아닌 경우
   *
   */
  @Transactional
  public Post updatePost(Long postId, PostUpdateDto updateDto) {
    Post targetPost = getPostByPostId(postId);
    if (!postPolicy.canUpdate(targetPost.getUser())) {
      throw new AccessDeniedException("failed to update post : invalid user or role");
    }
    targetPost.update(updateDto);
    return targetPost;
  }

  @Transactional
  public String updatePostAndGetPreviousImage(Long postId, PostUpdateDto updateDto) {
    Post targetPost = getPostByPostId(postId);
    if (!postPolicy.canUpdate(targetPost.getUser())) {
      throw new AccessDeniedException("failed to update post : invalid user or role");
    }
    String oldFileName = targetPost.getPreviewImageName();
    targetPost.update(updateDto);
    return oldFileName;
  }

  /**
   * 게시글 삭제
   *
   * @param postId 삭제할 게시글 ID
   * @throws AccessDeniedException 현재 사용자가 게시글 작성자가 아니고, admin 계정이 아닌 경우
   *
   */
  @Transactional
  public void deletePost(Long postId) {
    Post targetPost = getPostByPostId(postId);
    if (!postPolicy.canDelete(targetPost.getUser())) {
      throw new AccessDeniedException("failed to delete post : invalid user or role");
    }
    postRepository.deleteById(targetPost.getPostId());
  }

  /**
   * 특정 사용자의 게시글 목록 반환
   *
   */
  @Transactional(readOnly = true)
  public List<PostQuery.Detail> findUserPostList(User user, Pageable pageable) {
    return postRepositorySupport.findMyPostsByUser(user, pageable);
  }

  // 업로드 수 count 반환 메서드
  @Transactional(readOnly = true)
  public int countPost(String publicUserId) {
    return postRepository.countByUser_PublicUserId(publicUserId);
  }

  // 사용자가 카테고리에 저장한 게시글 목록 조회
  @Transactional(readOnly = true)
  public List<PostQuery.Detail> findUserSavedPosts(User user, Pageable pageable) {
    return postRepositorySupport.findBookmarkedPostsByUser(user, pageable);
  }

  @Transactional(readOnly = true)
  public List<PostQuery.Detail> findUserPreferredPosts(User user, Pageable pageable) {
    return postRepositorySupport.findUserPreferredPosts(user, pageable);
  }
}
