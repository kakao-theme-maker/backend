package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.komentum.global.properties.AuthProperty;
import com.komentum.global.properties.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@ExtendWith(MockitoExtension.class)
class CustomAuthorizationRequestResolverTest {

  private static final String AUTHORIZATION_REQUEST_URI = "/oauth2/authorization/kakao";

  @Mock
  private SecurityProperties securityProperties;

  @Mock
  private AuthProperty authProperty;

  private CustomAuthorizationRequestResolver resolver;

  private ClientRegistration kakaoRegistration;

  @BeforeEach
  void setUp() {
    // ClientRegistrationRepository 생성
    kakaoRegistration = ClientRegistration.withRegistrationId("kakao")
        .clientId("test-client-id")
        .clientSecret("test-client-secret")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://localhost:8080/login/oauth2/code/kakao")
        .authorizationUri("https://kauth.kakao.com/oauth/authorize")
        .tokenUri("https://kauth.kakao.com/oauth/token")
        .build();
    ClientRegistrationRepository clientRegistrationRepository =
        new InMemoryClientRegistrationRepository(kakaoRegistration);
    // CustomAuthorizationRequestResolver 생성
    resolver = new CustomAuthorizationRequestResolver(clientRegistrationRepository,
        securityProperties, authProperty);
  }

  @Test
  @DisplayName("redirect 파라미터가 허용된 origin이면 그대로 attribute에 저장한다")
  void resolve_whenRedirectIsAllowedOrigin_keepsRedirectValue() {
    // given
    given(securityProperties.getAllowedOriginList())
        .willReturn(new String[]{"http://localhost:5371"});
    MockHttpServletRequest request = new MockHttpServletRequest("GET", AUTHORIZATION_REQUEST_URI);
    request.setParameter(AuthProperty.OAUTH2_REDIRECT_PARAM,
        "http://localhost:5371/oauth/callback");
    // when
    OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request,
        kakaoRegistration.getRegistrationId());
    // then
    assertThat(authorizationRequest).isNotNull();
    assertThat(authorizationRequest.<String>getAttribute(
        AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE))
        .isEqualTo("http://localhost:5371/oauth/callback");
  }

  @Test
  @DisplayName("redirect 파라미터가 허용되지 않은 origin이면 기본 redirect url을 attribute에 저장한다")
  void resolve_whenRedirectIsNotAllowedOrigin_fallsBackToDefaultUrl() {
    // given
    String defaultRedirectUrl = "https://default.origin.com";
    given(securityProperties.getAllowedOriginList())
        .willReturn(new String[]{"http://localhost:5371"});
    given(authProperty.getOauth2RedirectUrl()).willReturn(defaultRedirectUrl);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", AUTHORIZATION_REQUEST_URI);
    request.setParameter(AuthProperty.OAUTH2_REDIRECT_PARAM, "https://evil.com/callback");
    // when
    OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request,
        kakaoRegistration.getRegistrationId());
    // then
    assertThat(authorizationRequest.<String>getAttribute(
        AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE))
        .isEqualTo(defaultRedirectUrl);
  }

  @Test
  @DisplayName("redirect 파라미터가 없으면 기본 redirect url을 attribute에 저장한다")
  void resolve_whenRedirectParamMissing_fallsBackToDefaultUrl() {
    // given
    String defaultRedirectUrl = "https://default.origin.com";
    given(authProperty.getOauth2RedirectUrl()).willReturn(defaultRedirectUrl);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", AUTHORIZATION_REQUEST_URI);
    // when
    OAuth2AuthorizationRequest authorizationRequest = resolver.resolve(request,
        kakaoRegistration.getRegistrationId());
    // then
    assertThat(authorizationRequest.<String>getAttribute(
        AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE))
        .isEqualTo(defaultRedirectUrl);
  }
}
