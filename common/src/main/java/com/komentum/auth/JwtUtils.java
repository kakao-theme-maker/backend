package com.komentum.auth;

import com.komentum.constants.AuthProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
@Profile("auth")
public class JwtUtils {

  private final Key SECRET_KEY;

  public JwtUtils(@Value("${jwt.secret}") String secret_key) {
    byte[] keyBytes = Decoders.BASE64.decode(secret_key);
    this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * generate token
   */
  private String generateToken(String email, Long expTime) {
    Claims claims = Jwts.claims().setSubject(email);
    claims.put("email", email);
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
  public String generateAccessToken(String email) {
    return generateToken(email, AuthProperty.ACCESS_TOKEN_EXPIRES_IN);
  }

  /**
   * generate refresh token
   */
  public String generateRefreshToken(String email) {
    return generateToken(email, AuthProperty.REFRESH_TOKEN_EXPIRES_IN);
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
   * get email from token
   */
  public String getEmail(String token) {
    try {
      return Jwts
          .parserBuilder()
          .setSigningKey(SECRET_KEY)
          .build()
          .parseClaimsJws(token)
          .getBody()
          .get("email", String.class);
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
}
