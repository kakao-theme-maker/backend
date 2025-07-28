package com.theme.post.service;

import com.theme.domain.User;
import com.theme.post.domain.Post;
import com.theme.post.dto.PostDto.PostCreateDto;
import com.theme.post.dto.PostDto.PostRawData;
import com.theme.post.dto.PostDto.PostUpdateDto;
import com.theme.post.repository.PostRepository;
import com.theme.repository.UserRepository;
import jakarta.ws.rs.NotFoundException;
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
  private final UserRepository userRepository;

  public List<PostRawData> getPosts(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return postRepository.getPreferPosts(pageable).stream().map(row -> {
      Post post = (Post) row[0];
      long prefer = (long) row[1];
      return new PostRawData(post, prefer);
    }).toList();
  }

  public PostRawData getPostById(Long postId) {
    Object[] rows = (Object[]) postRepository.getPreferPostsByPostId(postId);
    return new PostRawData((Post) rows[0], (long) rows[1]);
  }

  @Transactional
  public PostRawData createPost(PostCreateDto postCreateDto) {
    User targetUser = userRepository.findById(postCreateDto.getUserEmail())
        .orElseThrow(() -> new NotFoundException("User not found"));
    Post post = postRepository.save(Post.createTransient(postCreateDto, targetUser));
    return new PostRawData(post, 0L);
  }

  @Transactional
  public PostRawData updatePost(Long postId, PostUpdateDto postUpdateDto) {
    PostRawData target = getPostById(postId);
    Post targetPost = target.getPost();
    targetPost.update(postUpdateDto);
    target.setPost(postRepository.save(targetPost));
    return target;
  }

  @Transactional
  public void deletePost(Long postId) {
    PostRawData target = getPostById(postId);
    Post targetPost = target.getPost();
    postRepository.deleteById(targetPost.getPostId());
  }
}
