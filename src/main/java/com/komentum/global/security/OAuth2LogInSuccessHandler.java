package com.komentum.global.security;

import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.CustomOAuth2User;
import com.komentum.global.properties.AuthProperty;
import com.komentum.global.security.cookie.TokenCookieManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LogInSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtUtils jwtUtils;
  private final AuthProperty authProperty;
  private final TokenCookieManager tokenCookieManager;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    String accessToken = jwtUtils.generateAccessToken(oAuth2User.getUserIdentifier());
    String refreshToken = jwtUtils.generateRefreshToken(oAuth2User.getUserIdentifier());
    tokenCookieManager.addTokenOnCookie(response, accessToken, refreshToken);

    String redirectUrl = authProperty.getOauth2RedirectUrl();
    response.sendRedirect(redirectUrl);
  }
}
