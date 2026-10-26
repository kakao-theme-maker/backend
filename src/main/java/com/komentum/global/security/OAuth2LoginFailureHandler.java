package com.komentum.global.security;

import com.komentum.global.properties.AuthProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final OAuth2RedirectUriResolver oAuth2RedirectUriResolver;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    log.info("소셜 로그인 실패", exception);
    String redirectUrl = oAuth2RedirectUriResolver.resolve(request);
    String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
        .queryParam(AuthProperty.OAUTH2_ERROR_PARAM, "OAUTH2_LOGIN_FAILED")
        .build()
        .toUriString();
    response.sendRedirect(targetUrl);
  }
}
