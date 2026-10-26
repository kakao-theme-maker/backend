package com.komentum.user.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.cookie.TokenCookieManager;
import com.komentum.user.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GuestController {

  private final UserAuthService userAuthService;
  private final TokenCookieManager tokenCookieManager;

  /**
   * 게스트 토큰 발급 및 게스트 사용자 Entity 생성
   * */
  @PostMapping("/guest-tokens")
  @Operation(summary = "사용자에게 게스트 토큰을 발급/갱신한다", description = """
      <b>게스트 토큰을 생성한다.</b>
      <p>1. 사용자 인증 정보가 없으면, 게스트 사용자를 생성하고 토큰을 발급 한다.</p>
      <p>2. 사용자 인증 정보가 있고 DB에 없으면, 게스트 사용자를 재생성하고 토큰을 재발급한다.</p>
      <p>3. 사용자 인증 정보가 있고 DB에 있지만 GUEST 권한이 아니라면, 예외를 던진다.</p>
      """)
  public ResponseEntity<Void> createGuestToken(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      HttpServletResponse response) {
    String guestToken = userAuthService.createGuestToken(userDetails);
    tokenCookieManager.addGuestTokenOnCookie(response, guestToken);
    return ResponseEntity.ok().build();
  }
}
