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
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

  String identifier = "publicUserId";
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
  private String generateToken(String publicUserId, Long expTime) {
    Claims claims = Jwts.claims().setSubject(publicUserId);
    claims.put(identifier, publicUserId);
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
    return generateToken(publicUserId, authProperty.getAccessTokenExpiresIn());
  }

  /**
   * generate refresh token
   */
  public String generateRefreshToken(String publicUserId) {
    return generateToken(publicUserId, authProperty.getRefreshTokenExpiresIn());
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

  /**
   * extract token from request
   */
  public String resolveJwtToken(HttpRequest request) {
    try {
      String authorization = request.getHeaders().getFirst(AuthProperty.ACCESS_TOKEN_HEADER);
      if (authorization == null || !authorization.startsWith(AuthProperty.ACCESS_TOKEN_PREFIX)) {
        return null;
      }
      return authorization.substring(AuthProperty.ACCESS_TOKEN_PREFIX.length());
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
  public String resolveJwtToken(HttpServletRequest request) {
    try {
      String accessToken = extractAccessTokenFromHeader(request);
      if (accessToken == null) {
        accessToken = extractAccessTokenFromCookie(request);
      }
      return accessToken;
    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private String extractAccessTokenFromHeader(HttpServletRequest request) {
    String authorization = request.getHeader(AuthProperty.ACCESS_TOKEN_HEADER);
    if (authorization == null || !authorization.startsWith(AuthProperty.ACCESS_TOKEN_PREFIX)) {
      return null;
    }
    return authorization.substring(AuthProperty.ACCESS_TOKEN_PREFIX.length());
  }

  private String extractAccessTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }
    return Arrays.stream(cookies)
        .filter(c -> c.getName().equals(AuthProperty.ACCESS_TOKEN_COOKIE_NAME))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
