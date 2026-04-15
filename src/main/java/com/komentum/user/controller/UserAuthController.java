package com.komentum.user.controller;

import com.komentum.auth.JwtUtils;
import com.komentum.global.properties.AuthProperty;
import com.komentum.global.security.cookie.TokenCookieManager;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.SignUpRequestDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

  private final UserAuthService userAuthService;
  private final TokenCookieManager tokenCookieManager;
  private final JwtUtils jwtUtils;


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
      @RequestBody LocalLoginRequestDto localLoginRequestDto,
      HttpServletResponse response) {
    UserAuthResponse userAuthResponse = userAuthService.processLocalSignIn(localLoginRequestDto);
    tokenCookieManager.addTokenOnCookie(response, userAuthResponse.getAccessToken(),
        userAuthResponse.getRefreshToken());
    return ResponseEntity.ok(userAuthService.processLocalSignIn(localLoginRequestDto));
  }

  // 로컬 로그아웃 기능
  @PostMapping("/local/sign-out")
  @Operation(summary = "인증된 사용자가 로그아웃을 진행한다")
  public ResponseEntity<String> signOut(HttpServletRequest request, HttpServletResponse response) {
    String accessToken = jwtUtils.resolveJwtToken(request);
    userAuthService.handleLogout(accessToken);
    tokenCookieManager.removeTokenOnCookie(response);
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
