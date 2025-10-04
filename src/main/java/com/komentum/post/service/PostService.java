package com.komentum.post.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostDetail;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.PostSummary;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.TagRepository;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;

  @Transactional(readOnly = true)
  public List<PostSummary> getPostSummaries(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return postRepository.getPostSummary(pageable);
  }

  @Transactional(readOnly = true)
  public PostSummary getPostSummaryByPostId(Long postId) {
    return postRepository.getPostSummaryByPostId(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(Post.class, postId));
  }

  @Transactional(readOnly = true)
  public Post getPostByPostId(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new CustomEntityNotFoundException(Post.class, postId));
  }

  @Transactional
  public Post createPost(PostCreateDto postCreateDto, User targetUser) {
    return postRepository.save(Post.createTransient(postCreateDto, targetUser));
  }

  @Transactional
  public Post updatePost(Long postId, PostUpdateDto postUpdateDto) {
    Post targetPost = getPostByPostId(postId);
    targetPost.update(postUpdateDto);
    return targetPost;
  }

  @Transactional
  public void deletePost(Long postId) {
    Post targetPost = getPostByPostId(postId);
    postRepository.deleteById(targetPost.getPostId());
  }
}
