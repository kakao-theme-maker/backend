package com.komentum.global.security;

import com.komentum.global.properties.AuthProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * OAuth2AuthorizationRequest가 세션에서 제거되는 시점(인증 완료 시점)에
 * FE redirect 정보를 HttpServletRequest attribute로 옮겨,
 * 이후 이어지는 success/failure handler에서 세션 상태와 무관하게 읽을 수 있도록 한다.
 */
@Component
public class RedirectAwareAuthorizationRequestRepository implements
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> delegate =
      new HttpSessionOAuth2AuthorizationRequestRepository();

  /**
   * HttpServletRequest의 OAuth2AuthorizationRequest를 추출 및 반환한다
   * */
  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return delegate.loadAuthorizationRequest(request);
  }

  /**
   * HttpServletRequest와 HttpServletResponse에 authorizationRequest를 저장한다
   * */
  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
      HttpServletRequest request, HttpServletResponse response) {
    delegate.saveAuthorizationRequest(authorizationRequest, request, response);
  }

  /**
   * OAuth2AuthorizationRequest를 삭제 및 추출하고, HttpSerlvetRequest에 FE의 redirect uri를 저장한다
   * */
  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
      HttpServletResponse response) {
    OAuth2AuthorizationRequest authorizationRequest = delegate.removeAuthorizationRequest(request,
        response);
    if (authorizationRequest != null) {
      Object feRedirectUri = authorizationRequest.getAttribute(
          AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE);
      if (feRedirectUri != null) {
        request.setAttribute(AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE, feRedirectUri);
      }
    }
    return authorizationRequest;
  }
}
