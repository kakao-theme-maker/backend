package com.komentum.global.security;

import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.CustomOAuth2User;
import com.komentum.global.properties.AuthProperty;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LogInSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtils jwtUtils;
  private final AuthProperty authProperty;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    String accessToken = jwtUtils.generateAccessToken(oAuth2User.getUserIdentifier());
    String refreshToken = jwtUtils.generateRefreshToken(oAuth2User.getUserIdentifier());
    ResponseCookie accessTokenCookie = ResponseCookie
        .from(AuthProperty.accessTokenCookieName, accessToken)
        .httpOnly(true)
        .secure(authProperty.getWithHttps())
        .path("/")
        .maxAge(authProperty.getAccessTokenExpiresIn())
        .sameSite("Strict")
        .build();
    ResponseCookie refreshTokenCookie = ResponseCookie
        .from(AuthProperty.refreshTokenCookieName, refreshToken)
        .httpOnly(true)
        .secure(authProperty.getWithHttps())
        .path("/")
        .maxAge(authProperty.getRefreshTokenExpiresIn())
        .sameSite("Strict")
        .build();
    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());

    String redirectUrl = authProperty.getOauth2RedirectUrl();
    response.sendRedirect(redirectUrl);
  }
}
