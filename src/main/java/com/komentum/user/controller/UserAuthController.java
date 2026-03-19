package com.komentum.user.controller;

import com.komentum.global.properties.AuthProperty;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.SignUpRequestDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

  private final UserAuthService userAuthService;

  public UserAuthController(UserAuthService userAuthService) {
    this.userAuthService = userAuthService;
  }


  // Local 회원가입 기능
  @PostMapping("/local/sign-up")
  @Operation(summary = "인증되지 않은 사용자가 로컬 회원가입을 진행한다")
  public ResponseEntity<String> singUpLocal(
      @RequestBody SignUpRequestDto signUpRequestDto) {
    userAuthService.processLocalSignUp(signUpRequestDto);
    return ResponseEntity.ok("signup success");
  }

  // Local 로그인 기능
  @PostMapping("/local/sign-in")
  @Operation(summary = "인증되지 않은 사용자가 로컬 로그인을 진행한다")
  public ResponseEntity<UserAuthResponse> signInLocal(
      @RequestBody LocalLoginRequestDto localLoginRequestDto) {
    return ResponseEntity.ok(userAuthService.processLocalSignIn(localLoginRequestDto));
  }

  /**
   * 카카오 로그아웃 기능
   */
  @PostMapping("/kakao/logout")
  public ResponseEntity<String> logoutWithKakao(
      @RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String accessToken) {
    accessToken = accessToken.replace(AuthProperty.ACCESS_TOKEN_PREFIX, "");
    userAuthService.handleLogout(accessToken);
    return ResponseEntity.ok("logout success");
  }

  // 로컬 로그아웃 기능
  @PostMapping("/local/sign-out")
  @Operation(summary = "인증된 사용자가 로컬 로그아웃을 진행한다")
  public ResponseEntity<String> signOutLocal(
      @RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String accessToken) {
    accessToken = accessToken.replace(AuthProperty.ACCESS_TOKEN_PREFIX, "");
    userAuthService.handleLogout(accessToken);
    return ResponseEntity.ok("logout success");
  }


  /**
   * 토큰 재발급
   */
  @PostMapping("/token")
  @Operation(summary = "인증된 사용자의 refresh token으로 토큰을 재발급한다")
  public ResponseEntity<UserAuthResponse> generateToken(
      @RequestHeader(AuthProperty.ACCESS_TOKEN_HEADER) String refreshToken) {
    refreshToken = refreshToken.replace(AuthProperty.ACCESS_TOKEN_PREFIX, "");
    return ResponseEntity.ok(userAuthService.doRefreshTokenRotation(refreshToken));
  }
}
