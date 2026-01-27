package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final FileManager fileManager;

  @Transactional(readOnly = true)
  public Post getPostByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(Post.class, postId));
  }

  public List<Post> getPostsByUserEmail(String userEmail){
    return postRepository.findByUser_UserEmail(userEmail);
  }

  @Transactional
  public Post createPost(PostCreateDto postCreateDto, User author, String profileImageName) {
    return postRepository.save(Post.createTransient(postCreateDto, author, profileImageName));
  }

  @Transactional
  public Post updatePost(Long postId, User editor, PostUpdateDto updateDto) {
    Post targetPost = getPostByPostId(postId);
    if (!targetPost.getUser().equals(editor)) {
      throw new IllegalArgumentException("You are not authorized to update this board");
    }
    targetPost.update(updateDto);
    return targetPost;
  }

  @Transactional
  public void deletePost(Long postId) {
    Post targetPost = getPostByPostId(postId);
    postRepository.deleteById(targetPost.getPostId());
  }

  // 유저가 작성한 게시글 목록 조회 메서드
  public List<UserPostListResponseDto> findUserPostList(String userEmail){

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
