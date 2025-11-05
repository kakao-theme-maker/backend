package com.komentum.post.controller;

import com.komentum.global.dto.PageableRequestDto;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.dto.PostDto.ThemeBoardDetailDto;
import com.komentum.post.dto.PostDto.ThemeBoardPreviewDto;
import com.komentum.post.facade.ThemeBoardManagementFacade;
import com.komentum.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final ThemeBoardManagementFacade themeBoardManagementFacade;
  private final PostService postService;

  @GetMapping
  public ResponseEntity<List<ThemeBoardPreviewDto>> getPosts(
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.findThemeBoardPreviews(pageableRequestDto.toPageable()));
  }

  @GetMapping("/{postId}")
  public ResponseEntity<ThemeBoardDetailDto> getPost(@PathVariable Long postId) {
    return ResponseEntity.ok(themeBoardManagementFacade.findThemeBoardDetail(postId));
  }

  @PostMapping
  public ResponseEntity<ThemeBoardDetailDto> createPost(@RequestBody PostCreateDto createDto) {
    return ResponseEntity.ok(themeBoardManagementFacade.createThemeBoardWithTags(createDto));
  }

  @PutMapping("/{postId}")
  public ResponseEntity<ThemeBoardDetailDto> updatePost(@PathVariable Long postId,
      @RequestBody PostUpdateDto updateDto) {
    return ResponseEntity.ok(
        themeBoardManagementFacade.updateThemeBoardWithTags(postId, updateDto));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
    postService.deletePost(postId);
    return ResponseEntity.noContent().build();
  }
}
