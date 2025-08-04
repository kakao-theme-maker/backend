package com.komentum.user.service;

import com.komentum.auth.AuthProperty;
import com.komentum.user.redis.RedisSingleDataService;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final RedisSingleDataService redisSingleDataService;

  public TokenService(RedisSingleDataService redisSingleDataService) {
    this.redisSingleDataService = redisSingleDataService;
  }

  public String getAccessTokenKey(String email) {
    return "access_token_" + email;
  }

  public String getRefreshTokenKey(String email) {
    return "refresh_token_" + email;
  }

  public boolean saveAccessToken(String email, String accessToken) {
    return redisSingleDataService.set(getAccessTokenKey(email), accessToken,
        AuthProperty.ACCESS_TOKEN_EXPIRES_IN.intValue());
  }

  public boolean saveRefreshToken(String email, String refreshToken) {
    return redisSingleDataService.set(getRefreshTokenKey(email), refreshToken,
        AuthProperty.REFRESH_TOKEN_EXPIRES_IN.intValue());
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
