package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
  public Post createPost(PostCreateDto postCreateDto, User author) {
    return postRepository.save(Post.createTransient(postCreateDto, author));
  }

  @Transactional
  public Post updatePost(Long postId, ThemeBoardUpdateDto themeBoardUpdateDto) {
    Post targetPost = getPostByPostId(postId);
    targetPost.update(themeBoardUpdateDto);
    return targetPost;
  }

  @Transactional
  public void deletePost(Long postId) {
    Post targetPost = getPostByPostId(postId);
    postRepository.deleteById(targetPost.getPostId());
  }
}
