package com.theme.post.controller;

import com.theme.post.dto.PostDto.PostCreateDto;
import com.theme.post.dto.PostDto.PostResponse;
import com.theme.post.dto.PostDto.PostUpdateDto;
import com.theme.post.service.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @GetMapping
  public ResponseEntity<List<PostResponse>> getPosts(
      @RequestParam("pageNumber") Integer pageNumber, @RequestParam("pageSize") Integer pageSize) {
    pageNumber = pageNumber == null ? 0 : pageNumber;
    pageSize = pageSize == null ? 20 : pageSize;
    return ResponseEntity.ok(
        postService.getPosts(pageNumber, pageSize).stream().map(PostResponse::from)
            .toList());
  }

  @GetMapping("/{postId}")
  public ResponseEntity<PostResponse> getPost(@PathVariable Long postId) {
    return ResponseEntity.ok(PostResponse.from(postService.getPostById(postId)));
  }

  @PostMapping
  public ResponseEntity<PostResponse> createPost(@RequestBody PostCreateDto postDto) {
    return ResponseEntity.ok(PostResponse.from(postService.createPost(postDto)));
  }

  @PutMapping("/{postId}")
  public ResponseEntity<PostResponse> updatePost(@PathVariable Long postId,
      @RequestBody PostUpdateDto postDto) {
    return ResponseEntity.ok(PostResponse.from(postService.updatePost(postId, postDto)));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
    postService.deletePost(postId);
    return ResponseEntity.noContent().build();
  }
}
