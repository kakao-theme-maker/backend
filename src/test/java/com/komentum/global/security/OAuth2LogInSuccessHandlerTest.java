package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.CustomOAuth2User;
import com.komentum.global.security.cookie.TokenCookieManager;
import com.komentum.user.domain.User;
import com.komentum.user.service.TokenService;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class OAuth2LogInSuccessHandlerTest {

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private OAuth2RedirectUriResolver oAuth2RedirectUriResolver;

  @Mock
  private TokenCookieManager tokenCookieManager;

  @Mock
  private TokenService tokenService;

  private OAuth2LogInSuccessHandler successHandler;

  @BeforeEach
  void setUp() {
    successHandler = new OAuth2LogInSuccessHandler(jwtUtils, oAuth2RedirectUriResolver,
        tokenCookieManager, tokenService);
  }

  @Test
  @DisplayName("인증 성공 시 토큰을 발급하고 로그인을 시작한 FE로 resolve된 redirect url로 이동한다")
  void onAuthenticationSuccess_redirectsToResolvedUrl() throws Exception {
    // given
    User user = User.builder().publicUserId("public-user-id").build();
    CustomOAuth2User oAuth2User = new CustomOAuth2User(user, Map.of());
    Authentication authentication = new TestingAuthenticationToken(oAuth2User, null);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    // stub
    given(jwtUtils.generateAccessToken("public-user-id")).willReturn("access-token");
    given(jwtUtils.generateRefreshToken("public-user-id")).willReturn("refresh-token");
    given(oAuth2RedirectUriResolver.resolve(any()))
        .willReturn("http://localhost:5371/oauth/callback");
    // when
    successHandler.onAuthenticationSuccess(request, response, authentication);
    // then
    assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5371/oauth/callback");
    verify(tokenCookieManager).addTokenOnCookie(response, "access-token", "refresh-token");
  }
}
