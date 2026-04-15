package com.komentum.user.service;

import com.komentum.global.properties.AuthProperty;
import com.komentum.user.redis.RedisSingleDataService;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final RedisSingleDataService redisSingleDataService;
  private final AuthProperty authProperty;

  public TokenService(
      RedisSingleDataService redisSingleDataService,
      AuthProperty authProperty) {
    this.redisSingleDataService = redisSingleDataService;
    this.authProperty = authProperty;
  }

  public String getAccessTokenKey(String email) {
    return "access_token_" + email;
  }

  public String getRefreshTokenKey(String email) {
    return "refresh_token_" + email;
  }

  public boolean saveAccessToken(String email, String accessToken) {
    return redisSingleDataService.set(getAccessTokenKey(email), accessToken,
        authProperty.getAccessTokenExpiresIn().intValue());
  }

  public boolean saveRefreshToken(String email, String refreshToken) {
    return redisSingleDataService.set(getRefreshTokenKey(email), refreshToken,
        authProperty.getRefreshTokenExpiresIn().intValue());
  }

  public boolean saveAccessAndRefreshToken(String email, String accessToken, String refreshToken) {
    boolean success1 = saveAccessToken(email, accessToken);
    boolean success2 = saveRefreshToken(email, refreshToken);
    return success1 && success2;
  }

  public String getAccessToken(String email) {
    return redisSingleDataService.get(getAccessTokenKey(email));
  }

  public String getRefreshToken(String email) {
    return redisSingleDataService.get(getRefreshTokenKey(email));
  }

  public boolean deleteAccessToken(String email) {
    return redisSingleDataService.delete(getAccessTokenKey(email));
  }

  public boolean deleteRefreshToken(String email) {
    return redisSingleDataService.delete(getRefreshTokenKey(email));
  }
}
