package com.komentum.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException {
    log.info("소셜 로그인 실패", exception);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
    response.getWriter().write("""
        {
          "error": "OAUTH2_LOGIN_FAILED",
          "message": "소셜 로그인 실패"
        }
        """);
  }
}
