package com.komentum.global.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "auth")
public class AuthProperty {

  // 인증 관련 상수
  public static final String ACCESS_TOKEN_PREFIX = "Bearer";
  public static final String ACCESS_TOKEN_HEADER = "Authorization";
  public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  // 인증 관련 환경 변수
  private final Long accessTokenExpiresIn;
  private final Long refreshTokenExpiresIn;
  private final String oauth2RedirectUrl;
  private final Boolean withHttps;
}
