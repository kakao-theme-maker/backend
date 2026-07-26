package com.komentum.user.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.user.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

  private final FollowService followService;

  @PutMapping("/{public_user_id}/follow")
  @Operation(summary = "현재 인증된 사용자가 대상 사용자를 팔로우한다")
  public ResponseEntity<Void> follow(
      @Parameter(description = "팔로우할 사용자의 공개 ID")
      @PathVariable("public_user_id") String followeePublicUserId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    followService.follow(userDetails.getUsername(), followeePublicUserId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{public_user_id}/follow")
  @Operation(summary = "현재 인증된 사용자가 대상 사용자를 팔로우 해제한다")
  public ResponseEntity<Void> unfollow(
      @Parameter(description = "팔로우 해제할 사용자의 공개 ID")
      @PathVariable("public_user_id") String followeePublicUserId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    followService.unfollow(userDetails.getUsername(), followeePublicUserId);
    return ResponseEntity.noContent().build();
  }
}
