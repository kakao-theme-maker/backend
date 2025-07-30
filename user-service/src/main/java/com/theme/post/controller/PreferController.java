package com.theme.post.controller;

import com.theme.post.dto.PreferDto.PreferCreateDto;
import com.theme.post.dto.PreferDto.PreferDeleteDto;
import com.theme.post.service.PreferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PreferController {

  private final PreferService preferService;

  @GetMapping("/{postId}/prefer")
  public ResponseEntity<Long> getPreferCount(@PathVariable Long postId) {
    return ResponseEntity.ok(preferService.getPreferByPost(postId));
  }

  @PostMapping("/{postId}/prefer")
  public ResponseEntity<Void> savePrefer(@PathVariable Long postId,
      @RequestBody PreferCreateDto createDto) {
    preferService.savePrefer(postId, createDto);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{postId}/prefer")
  public ResponseEntity<Void> deletePrefer(@PathVariable Long postId,
      @RequestBody PreferDeleteDto deleteDto) {
    preferService.deletePrefer(postId, deleteDto);
    return ResponseEntity.noContent().build();
  }
}
