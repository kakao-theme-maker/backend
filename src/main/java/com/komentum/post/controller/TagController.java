package com.komentum.post.controller;

import com.komentum.post.dto.TagDto.TagBatchCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.service.TagService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class TagController {

  private final TagService tagService;

  @GetMapping("/{postId}/tags")
  public ResponseEntity<List<TagResponse>> getTagsByTagId(@PathVariable Long postId) {
    return ResponseEntity.ok(
        tagService.findAllByPostId(postId).stream().map(TagResponse::from).toList());
  }

  @PostMapping("/{postId}/tags")
  public ResponseEntity<List<TagResponse>> createTag(@PathVariable Long postId,
      @RequestBody TagBatchCreateDto tagBatchCreateDto) {
    return ResponseEntity.ok(
        tagService.createTag(postId, tagBatchCreateDto).stream().map(TagResponse::from).toList());
  }

  @PutMapping("/tags/{tagId}")
  public ResponseEntity<TagResponse> updateTag(@PathVariable Long tagId,
      @RequestBody TagUpdateDto tagUpdateDto) {
    return ResponseEntity.ok(TagResponse.from(tagService.updateTag(tagId, tagUpdateDto)));
  }

  @DeleteMapping("/tags/{tagId}")
  public ResponseEntity<TagResponse> deleteTag(@PathVariable Long tagId) {
    tagService.deleteTag(tagId);
    return ResponseEntity.noContent().build();
  }
}
