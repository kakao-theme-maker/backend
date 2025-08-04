package com.komentum.controller;

import com.komentum.constants.AuthProperty;
import com.komentum.dto.UserAuthRequest;
import com.komentum.dto.UserAuthResponse;
import com.komentum.service.UserAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserAuthController {

  private final UserAuthService userAuthService;

  public UserAuthController(UserAuthService userAuthService) {
    this.userAuthService = userAuthService;
  }

  /**
   * 카카오 로그인 기능
   *
   * @param userAuthRequest 사용자 auth code
   * @return 토큰들을 ResponseEntity 로 감싸서 반환
   */
  @PostMapping("/auth/kakao/login")
  public Mono<ResponseEntity<UserAuthResponse>> loginWithKakao(
      @RequestBody UserAuthRequest userAuthRequest) {
    return userAuthService.processKakaoAuth(userAuthRequest.getAuthCode())
        .map(ResponseEntity::ok);
  }

  /**
   * 카카오 로그아웃 기능
   */
  @PostMapping("/auth/kakao/logout")
  public ResponseEntity<String> logoutWithKakao(
      @RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String accessToken) {
    accessToken = accessToken.replace(AuthProperty.ACCESS_TOKEN_PREFIX, "");
    userAuthService.handleLogout(accessToken);
    return ResponseEntity.ok("logout success");
  }

  /**
   * 토큰 재발급
   */
  @PostMapping("/auth/token")
  public ResponseEntity<UserAuthResponse> generateToken(
      @RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String refreshToken) {
    refreshToken = refreshToken.replace(AuthProperty.ACCESS_TOKEN_PREFIX, "");
    return ResponseEntity.ok(userAuthService.doRefreshTokenRotation(refreshToken));
  }
}
