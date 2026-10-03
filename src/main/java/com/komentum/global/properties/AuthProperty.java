package com.komentum.global.properties;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "auth")
public class AuthProperty {

  // 인증 관련 상수
  public static final String ACCESS_TOKEN_PREFIX = "Bearer";
  public static final String ACCESS_TOKEN_HEADER = "Authorization";
  public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  // OAuth2 로그인 시작 FE로 redirect 하기 위한 상수
  public static final String OAUTH2_REDIRECT_PARAM = "redirect";
  public static final String OAUTH2_ERROR_PARAM = "error";
  public static final String OAUTH2_FE_REDIRECT_URI_ATTRIBUTE = "OAUTH2_FE_REDIRECT_URI";
  // 인증 관련 환경 변수
  private final Long accessTokenExpiresIn;
  private final Long refreshTokenExpiresIn;
  private final String oauth2RedirectUrl;
  private final Boolean withHttps;
  private final String sameSite;

  /**
   * SameSite가 None이라면 withHttps는 반드시 true이어야 한다
   * SameSite가 None이 아니라면 withHttps의 값은 상관 없다.
   * */
  @AssertTrue(message = "SameSite=None must be used with secure=true")
  public boolean isSameSiteNoneWithSecure() {
    return !"None".equalsIgnoreCase(sameSite) || withHttps;
  }
}
