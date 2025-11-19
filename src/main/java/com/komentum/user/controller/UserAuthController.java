package com.komentum.user.controller;

import com.komentum.auth.AuthProperty;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.UserAuthRequest;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.service.UserAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

  // Local 회원가입 기능
  @PostMapping("/auth/local/sign-up")
  public ResponseEntity<String> singUpLocal(
          @RequestBody LocalLoginRequestDto localLoginRequestDto){
    userAuthService.processLocalSingUp(localLoginRequestDto);
    return ResponseEntity.ok("signup success");
  }

  // Local 로그인 기능
  @PostMapping("/auth/local/sign-in")
  public ResponseEntity<UserAuthResponse> signInLocal(
          @RequestBody LocalLoginRequestDto localLoginRequestDto){
    return ResponseEntity.ok(userAuthService.processLocalsignIn(localLoginRequestDto));
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
