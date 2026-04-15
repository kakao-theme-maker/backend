package com.komentum.global.security.cookie;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.properties.AuthProperty;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class TokenCookieManagerTest {

  TokenCookieManager tokenCookieManager;

  private final AuthProperty authProperty = new AuthProperty(
      1000L,
      1000L,
      "http://localhost:3000",
      false
  );

  @BeforeEach
  public void setUp() {
    this.tokenCookieManager = new TokenCookieManager(authProperty);
  }

  private void assertTokenCookie(Cookie tokenCookie, String expectedValue) {
    assertThat(tokenCookie).isNotNull();
    assertThat(tokenCookie.isHttpOnly()).isTrue();
    assertThat(tokenCookie.getValue()).isEqualTo(expectedValue);
  }

  @Test
  void addTokenOnCookie() {
    // given
    MockHttpServletResponse response = new MockHttpServletResponse();
    String accessToken = "access-token";
    String refreshToken = "refresh-token";
    // when
    tokenCookieManager.addTokenOnCookie(response, accessToken, refreshToken);
    // then
    Cookie accessTokenCookie = response.getCookie(AuthProperty.ACCESS_TOKEN_COOKIE_NAME);
    Cookie refreshTokenCookie = response.getCookie(AuthProperty.REFRESH_TOKEN_COOKIE_NAME);
    assertTokenCookie(accessTokenCookie, accessToken);
    assertTokenCookie(refreshTokenCookie, refreshToken);
  }

  @Test
  void removeTokenOnCookie() {
    // given
    MockHttpServletResponse response = new MockHttpServletResponse();
    // when
    tokenCookieManager.removeTokenOnCookie(response);
    // then
    Cookie accessTokenCookie = response.getCookie(AuthProperty.ACCESS_TOKEN_COOKIE_NAME);
    Cookie refreshTokenCookie = response.getCookie(AuthProperty.REFRESH_TOKEN_COOKIE_NAME);
    assertThat(accessTokenCookie).isNotNull();
    assertThat(refreshTokenCookie).isNotNull();
    assertThat(accessTokenCookie.getMaxAge()).isEqualTo(0);
    assertThat(refreshTokenCookie.getMaxAge()).isEqualTo(0);
  }
}