package com.komentum.post.service;

import com.komentum.post.domain.Post;
import com.komentum.post.domain.Tag;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostDetail;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.post.repository.TagRepository;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;

  public List<PostDetail> getPosts(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    List<PostDetail> postDetails = postRepository.getPostDetailMappings(pageable).stream()
        .map(PostDetail::from).toList();
    List<Long> postIds = postDetails.stream()
        .map(post -> post.getPost().getPostId()).toList();
    Map<Post, List<Tag>> tagsByPost = tagRepository.findAllByPostIds(postIds).stream()
        .collect(Collectors.groupingBy(Tag::getPost));
    postDetails.forEach(post -> post.setTags(tagsByPost.get(post.getPost())));
    return postDetails;
  }

  public PostDetail getPostById(Long postId) {
    PostDetail postDetail = PostDetail.from(postRepository.getPreferPostsByPostId(postId));
    List<Tag> tagByPost = tagRepository.findAllByPostIds(List.of(postDetail.getPost().getPostId()));
    postDetail.setTags(tagByPost);
    return postDetail;
  }

  @Transactional
  public PostDetail createPost(PostCreateDto postCreateDto) {
    User targetUser = userRepository.findById(postCreateDto.getUserEmail())
        .orElseThrow(() -> new RuntimeException("User not found"));
    Post post = postRepository.save(Post.createTransient(postCreateDto, targetUser));
    return PostDetail.from(post);
  }

  @Transactional
  public PostDetail updatePost(Long postId, PostUpdateDto postUpdateDto) {
    PostDetail target = getPostById(postId);
    Post targetPost = target.getPost();
    targetPost.update(postUpdateDto);
    target.setPost(postRepository.save(targetPost));
    return target;
  }

  @Transactional
  public void deletePost(Long postId) {
    PostDetail target = getPostById(postId);
    Post targetPost = target.getPost();
    postRepository.deleteById(targetPost.getPostId());
  }
}
