package com.komentum.global.security.cookie;

import com.komentum.global.properties.AuthProperty;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCookieManager {

  private final AuthProperty authProperty;

  private ResponseCookie generateAccessTokenCookie(String accessToken, Long maxAge) {
    return ResponseCookie
        .from(AuthProperty.ACCESS_TOKEN_COOKIE_NAME, accessToken)
        .httpOnly(true)
        .secure(authProperty.getWithHttps())
        .path("/")
        .maxAge(maxAge)
        .sameSite("Strict")
        .build();
  }

  private ResponseCookie generateRefreshTokenCookie(String refreshToken, Long maxAge) {
    return ResponseCookie
        .from(AuthProperty.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(authProperty.getWithHttps())
        .path("/")
        .maxAge(maxAge)
        .sameSite("Strict")
        .build();
  }

  /**
   * 로그인 시 쿠키에 토큰을 추가한다
   * */
  public void addTokenOnCookie(
      HttpServletResponse response,
      String accessToken,
      String refreshToken
  ) {
    ResponseCookie accessTokenCookie = generateAccessTokenCookie(accessToken,
        authProperty.getAccessTokenExpiresIn());
    ResponseCookie refreshTokenCookie = generateRefreshTokenCookie(refreshToken,
        authProperty.getRefreshTokenExpiresIn());
    response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
  }

  /**
   * 로그아웃 시 쿠키에서 토큰을 제거한다
   * */
  public void removeTokenOnCookie(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, generateAccessTokenCookie("", 0L).toString());
    response.addHeader(HttpHeaders.SET_COOKIE, generateRefreshTokenCookie("", 0L).toString());
  }
}
