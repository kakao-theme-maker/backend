package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  @Transactional(readOnly = true)
  public Post getPostByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(Post.class, postId));
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

  // 업로드 수 count 반환 메서드
  public int countPost(String email){
    return postRepository.countByUser_UserEmail(email);
  }
}
