package com.komentum.auth;

import com.komentum.global.properties.AuthProperty;
import com.komentum.global.properties.JwtProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

  String identifier = "publicUserId";
  private static final String TOKEN_TYPE_KEY = "tokenType";
  private final Key SECRET_KEY;
  private final AuthProperty authProperty;

  public JwtUtils(JwtProperty jwtProperty, AuthProperty authProperty) {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperty.getSecret());
    this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    this.authProperty = authProperty;
  }

  /**
   * generate token
   */
  private String generateToken(String publicUserId, Long expTime, TokenType tokenType) {
    Claims claims = Jwts.claims().setSubject(publicUserId);
    claims.put(identifier, publicUserId);
    claims.put(TOKEN_TYPE_KEY, tokenType.name());
    ZonedDateTime issuedDate = ZonedDateTime.now();
    ZonedDateTime expiresDate = issuedDate.plusSeconds(expTime);
    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(Date.from(issuedDate.toInstant()))
        .setExpiration(Date.from(expiresDate.toInstant()))
        .signWith(this.SECRET_KEY)
        .compact();
  }

  /**
   * generate access token
   */
  public String generateAccessToken(String publicUserId) {
    return generateToken(publicUserId, authProperty.getAccessTokenExpiresIn(), TokenType.ACCESS);
  }

  /**
   * generate refresh token
   */
  public String generateRefreshToken(String publicUserId) {
    return generateToken(publicUserId, authProperty.getRefreshTokenExpiresIn(), TokenType.REFRESH);
  }

  /**
   * validate token
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      log.error(e.getMessage());
      return false;
    }
  }

  /**
   * get userId from token
   */
  public String getUserId(String token) {
    try {
      return Jwts
          .parserBuilder()
          .setSigningKey(SECRET_KEY)
          .build()
          .parseClaimsJws(token)
          .getBody()
          .get(identifier, String.class);
    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public String getTokenType(String token) {
    try {
      return Jwts
          .parserBuilder()
          .setSigningKey(SECRET_KEY)
          .build()
          .parseClaimsJws(token)
          .getBody()
          .get(TOKEN_TYPE_KEY, String.class);
    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

  /**
   * extract token from HttpServletRequest
   *
   * @param request HttpServletRequest
   * @return jwt token without prefix or null if token not exists
   */
  public String resolveToken(HttpServletRequest request, String cookieName) {
    try {
      String token = extractTokenFromHeader(request);
      if (token == null) {
        token = extractTokenFromCookie(request, cookieName);
      }
      return token;
    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

  /**
   * 헤더에서 토큰를 추출하고, 없으면 null을 반환한다
   * */
  private String extractTokenFromHeader(HttpServletRequest request) {
    String authorization = request.getHeader(AuthProperty.ACCESS_TOKEN_HEADER);
    if (authorization == null || !authorization.startsWith(AuthProperty.ACCESS_TOKEN_PREFIX)) {
      return null;
    }
    return authorization.substring(AuthProperty.ACCESS_TOKEN_PREFIX.length());
  }

  /**
   * 쿠키에서 토큰을 추출하고, 없으면 null을 반환한다
   * */
  private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(c -> c.getName().equals(cookieName))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }

  /**
   * 토큰이 ACCESS_TOKEN인지 확인한다
   * */
  public boolean isAccessToken(String token) {
    return token != null && TokenType.ACCESS.name().equals(getTokenType(token));
  }

  /**
   * 토큰이 REFRESH_TOKEN인지 확인한다
   * */
  public boolean isRefreshToken(String token) {
    return token != null && TokenType.REFRESH.name().equals(getTokenType(token));
  }
}
