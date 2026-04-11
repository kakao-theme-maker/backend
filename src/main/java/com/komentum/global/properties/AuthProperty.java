package com.komentum.global.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "auth")
public class AuthProperty {

  public static final String accessTokenPrefix = "Bearer";
  public static final String accessTokenHeader = "Authorization";
  public static final String accessTokenCookieName = "access_token";
  public static final String refreshTokenCookieName = "refresh_token";
  private final Long accessTokenExpiresIn;
  private final Long refreshTokenExpiresIn;
  private final String oauth2RedirectUrl;
  private final Boolean withHttps;
}
