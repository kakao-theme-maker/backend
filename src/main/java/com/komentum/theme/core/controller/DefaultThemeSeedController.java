package com.komentum.theme.core.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.theme.core.service.seeder.DefaultThemeSeeder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("unused-api")
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "Theme Management", description = "테마 관리 API")
public class DefaultThemeSeedController {

  private final DefaultThemeSeeder defaultThemeSeeder;

  /**
   * 인증된 root 사용자를 기준으로 디폴트 테마를 시딩한다.
   *
   * @param userDetails 현재 사용자 인증 정보
   * @return 본문이 없는 성공 응답
   */
  @PostMapping("/default/seed")
  @Operation(summary = "root user가 디폴트 테마를 시딩한다")
  public ResponseEntity<Void> seedDefaultTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    defaultThemeSeeder.seedDefaultThemes(userDetails.getPublicUserId());
    return ResponseEntity.ok().build();
  }
}
