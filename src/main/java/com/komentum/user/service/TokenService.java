package com.komentum.user.service;

import com.komentum.global.properties.AuthProperty;
import com.komentum.user.redis.RedisSingleDataService;
import java.util.List;
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

  public String getAccessTokenKey(String userIdentifier) {
    return "access_token_" + userIdentifier;
  }

  public String getRefreshTokenKey(String userIdentifier) {
    return "refresh_token_" + userIdentifier;
  }

  public boolean saveAccessToken(String userIdentifier, String accessToken) {
    return redisSingleDataService.set(getAccessTokenKey(userIdentifier), accessToken,
        authProperty.getAccessTokenExpiresIn().intValue());
  }

  public boolean saveRefreshToken(String userIdentifier, String refreshToken) {
    return redisSingleDataService.set(getRefreshTokenKey(userIdentifier), refreshToken,
        authProperty.getRefreshTokenExpiresIn().intValue());
  }

  public boolean saveAccessAndRefreshToken(String userIdentifier, String accessToken,
      String refreshToken) {
    boolean success1 = saveAccessToken(userIdentifier, accessToken);
    boolean success2 = saveRefreshToken(userIdentifier, refreshToken);
    return success1 && success2;
  }

  /**
   * 저장된 토큰과 prevRefreshToken이 일치하면 토큰을 교체하고, 그렇지 않다면 교체하지 않는다
   * @param userIdentifier 사용자 식별자
   * @param prevRefreshToken 현재 사용하고 있는 refresh token
   * @param newRefreshToken 새로 사용할 refresh token
   * */
  public boolean rotateRefreshToken(String userIdentifier, String prevRefreshToken,
      String newRefreshToken) {
    String key = getRefreshTokenKey(userIdentifier);
    // 동시성 문제 해결 : Lua Script 실행 ( 연속 2회 요청 등의 문제 대비 )
    String script = """
        local current = redis.call("GET", KEYS[1])
        if current == ARGV[1] then
            redis.call("SET", KEYS[1], ARGV[2], "EX", ARGV[3])
            return 1
        else
            return 0
        end
        """;
    // Lua Script 실행
    Long result = redisSingleDataService.executeLua(
        script,
        List.of(key),
        prevRefreshToken,
        newRefreshToken,
        String.valueOf(authProperty.getRefreshTokenExpiresIn())
    );
    return result != null && result == 1L;
  }

  public String getAccessToken(String email) {
    return redisSingleDataService.get(getAccessTokenKey(email));
  }

  public String getRefreshToken(String userIdentifier) {
    return redisSingleDataService.get(getRefreshTokenKey(userIdentifier));
  }

  public boolean deleteAccessToken(String userIdentifier) {
    return redisSingleDataService.delete(getAccessTokenKey(userIdentifier));
  }

  public boolean deleteRefreshToken(String userIdentifier) {
    return redisSingleDataService.delete(getRefreshTokenKey(userIdentifier));
  }
}
