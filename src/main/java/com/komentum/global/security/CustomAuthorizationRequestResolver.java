package com.komentum.global.security;

import com.komentum.global.properties.AuthProperty;
import com.komentum.global.properties.SecurityProperties;
import com.komentum.global.utils.RegexValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * 로그인을 시작한 FE의 redirect 파라미터를 검증하여 OAuth2AuthorizationRequest의 attribute로 보관한다.
 * 허용된 origin이 아니거나 값이 없으면 기본 redirect URL(auth.oauth2-redirect-url)을 사용한다.
 * 이 attribute는 세션에 저장되었다가 인증 완료 시 RedirectAwareAuthorizationRequestRepository를 통해
 * request attribute로 옮겨져 OAuth2LogInSuccessHandler / OAuth2LoginFailureHandler에서 사용된다.
 */
@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
  private final SecurityProperties securityProperties;
  private final AuthProperty authProperty;

  public CustomAuthorizationRequestResolver(
      ClientRegistrationRepository clientRegistrationRepository,
      SecurityProperties securityProperties,
      AuthProperty authProperty) {
    this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
    this.securityProperties = securityProperties;
    this.authProperty = authProperty;
  }


  /**
   * FE의 카카오 로그인 요청을 기반으로 OAuth2AuthorizationRequest를 생성한다
   * 이 때 request의 FE redirect URL을 OAuth2AuthorizationRequest에 주입한다
   * @param request 서블릿 요청 객체
   * */
  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    return customizeAuthorizationRequest(request, defaultResolver.resolve(request));
  }

  /**
   * FE의 카카오 로그인 요청을 기반으로 OAuth2AuthorizationRequest를 생성한다
   * 이 때 request의 FE redirect URL을 OAuth2AuthorizationRequest에 주입한다
   * @param request 서블릿 요청 객체
   * @param registrationId 소셜 로그인 제공 서비스 ID ( KAKAO, GOOGLE 등 )
   * */
  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
    return customizeAuthorizationRequest(request, defaultResolver.resolve(request, registrationId));
  }

  /**
   * 디폴트 OAuth2AuthorizationRequest에 추가 정보를 주입 및 반환한다
   * */
  private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request,
      OAuth2AuthorizationRequest authorizationRequest) {
    if (authorizationRequest == null) {
      return null;
    }
    // servlet request에서 FE redirect url 추출
    String feRedirectUri = resolveFeRedirectUri(
        request.getParameter(AuthProperty.OAUTH2_REDIRECT_PARAM));
    // OAuth2AuthorizationRequest에 FE redirect url 주입
    return OAuth2AuthorizationRequest.from(authorizationRequest)
        .attributes(attributes ->
            attributes.put(AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE, feRedirectUri))
        .build();
  }

  /**
   * redirectParam이 유효한지 확인한다
   * NULL/blank 검사, allowed-origin여부인지 확인하고, 유효하지 않다면 디폴트 redirect url을 반환한다
   * */
  private String resolveFeRedirectUri(String redirectParam) {
    if (redirectParam == null || redirectParam.isBlank()) {
      return authProperty.getOauth2RedirectUrl();
    }
    try {
      // redirectParam to origin
      URI uri = new URI(redirectParam);
      String origin = uri.getPort() == -1 ?
          uri.getScheme() + "://" + uri.getHost() :
          uri.getScheme() + "://" + uri.getHost() + ":" + uri.getPort();
      // validate origin
      if (RegexValidator.isAllowedOrigin(origin, securityProperties.getAllowedOriginList())) {
        return redirectParam;
      }
      log.warn("[Social Login] 허용되지 않은 OAuth2 redirect origin 요청: {}", origin);
    } catch (URISyntaxException e) {
      log.warn("[Social Login] 유효하지 않은 OAuth2 redirect 파라미터: {}", redirectParam);
    }
    return authProperty.getOauth2RedirectUrl();
  }
}
