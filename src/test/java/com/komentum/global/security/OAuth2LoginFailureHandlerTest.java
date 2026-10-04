package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.komentum.global.properties.AuthProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginFailureHandlerTest {

  @Mock
  private OAuth2RedirectUriResolver oAuth2RedirectUriResolver;

  private OAuth2LoginFailureHandler failureHandler;

  @BeforeEach
  void setUp() {
    failureHandler = new OAuth2LoginFailureHandler(oAuth2RedirectUriResolver);
  }

  @Test
  @DisplayName("로그인을 시작한 FE로 error 쿼리 파라미터를 붙여 redirect한다")
  void onAuthenticationFailure_redirectsToFeWithErrorParam() throws Exception {
    // given
    given(oAuth2RedirectUriResolver.resolve(any()))
        .willReturn("http://localhost:5371/oauth/callback");
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    AuthenticationException exception = new OAuth2AuthenticationException(
        new OAuth2Error("invalid_request"), "실패");
    // when
    failureHandler.onAuthenticationFailure(request, response, exception);
    // then
    assertThat(response.getRedirectedUrl())
        .isEqualTo("http://localhost:5371/oauth/callback?" + AuthProperty.OAUTH2_ERROR_PARAM
            + "=OAUTH2_LOGIN_FAILED");
  }
}
