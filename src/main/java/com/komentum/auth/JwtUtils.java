package com.komentum.auth;

import com.komentum.global.properties.AuthProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtils {

  String identifier = "publicUserId";
  private final Key SECRET_KEY;

  public JwtUtils(@Value("${jwt.secret}") String secret_key) {
    byte[] keyBytes = Decoders.BASE64.decode(secret_key);
    this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
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
    return generateToken(publicUserId, AuthProperty.ACCESS_TOKEN_EXPIRES_IN);
  }

  /**
   * generate refresh token
   */
  public String generateRefreshToken(String publicUserId) {
    return generateToken(publicUserId, AuthProperty.REFRESH_TOKEN_EXPIRES_IN);
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
      String authorization = request.getHeader(AuthProperty.ACCESS_TOKEN_HEADER);
      if (authorization == null || !authorization.startsWith(AuthProperty.ACCESS_TOKEN_PREFIX)) {
        return null;
      }
      return authorization.substring(AuthProperty.ACCESS_TOKEN_PREFIX.length());
    } catch (Exception e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
