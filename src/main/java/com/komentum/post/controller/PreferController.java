package com.komentum.post.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.post.facade.PreferManagementFacade;
import com.komentum.post.service.PreferService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PreferController {

  private final PreferManagementFacade preferManagementFacade;
  private final PreferService preferService;

  @GetMapping("/{post_id}/prefer")
  @Operation(summary = "인증된 사용자가 ID=post_id인 게시글의 추천 수를 조회한다")
  public ResponseEntity<Long> getPreferCount(@PathVariable("post_id") Long postId) {
    return ResponseEntity.ok(preferService.getPreferByPost(postId));
  }

  @PostMapping("/{post_id}/prefer")
  @Operation(summary = "인증된 사용자가 ID=post_id인 게시글을 추천한다")
  public ResponseEntity<Void> savePrefer(@PathVariable("post_id") Long postId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    preferManagementFacade.addPreferToPost(postId, userDetails.getUsername());
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{postId}/prefer")
  @Operation(summary = "인증된 사용자가 ID=post_id인 게시글을 추천 해제한다")
  public ResponseEntity<Void> deletePrefer(@PathVariable Long postId,
      @RequestBody CustomUserDetails userDetails) {
    preferManagementFacade.deletePreferFromPost(postId, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }
}
