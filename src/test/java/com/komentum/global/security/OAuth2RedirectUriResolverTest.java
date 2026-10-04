package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.komentum.global.properties.AuthProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class OAuth2RedirectUriResolverTest {

  @Mock
  private AuthProperty authProperty;

  private OAuth2RedirectUriResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new OAuth2RedirectUriResolver(authProperty);
  }

  @Test
  @DisplayName("request attribute에 FE redirect uri가 있으면 그 값을 반환한다")
  void resolve_whenAttributePresent_returnsAttributeValue() {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(AuthProperty.OAUTH2_FE_REDIRECT_URI_ATTRIBUTE,
        "http://localhost:5371/oauth/callback");
    // when
    String result = resolver.resolve(request);
    // then
    assertThat(result).isEqualTo("http://localhost:5371/oauth/callback");
  }

  @Test
  @DisplayName("request attribute가 없으면 기본 redirect url을 반환한다")
  void resolve_whenAttributeMissing_returnsDefaultUrl() {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    given(authProperty.getOauth2RedirectUrl()).willReturn("http://localhost:3000");
    // when
    String result = resolver.resolve(request);
    // then
    assertThat(result).isEqualTo("http://localhost:3000");
  }
}
