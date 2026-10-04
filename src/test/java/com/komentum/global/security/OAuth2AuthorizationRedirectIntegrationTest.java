package com.komentum.global.security;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.komentum.global.properties.AuthProperty;
import com.komentum.test.config.EnableTestProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class OAuth2AuthorizationRedirectIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("허용된 origin의 redirect 파라미터와 함께 카카오 로그인을 시작해도 정상적으로 인가 서버로 redirect된다")
  void startKakaoLogin_withAllowedRedirect_redirectsToKakaoAuthorizationServer() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorization/kakao")
            .param(AuthProperty.OAUTH2_REDIRECT_PARAM, "http://localhost:3000/oauth/callback"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location",
            startsWith("https://kauth.kakao.com/oauth/authorize")));
  }

  @Test
  @DisplayName("허용되지 않은 origin의 redirect 파라미터가 있어도 카카오 로그인 시작 자체는 실패하지 않는다")
  void startKakaoLogin_withDisallowedRedirect_stillRedirectsToKakaoAuthorizationServer()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorization/kakao")
            .param(AuthProperty.OAUTH2_REDIRECT_PARAM, "https://evil.com/callback"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location",
            startsWith("https://kauth.kakao.com/oauth/authorize")));
  }

  @Test
  @DisplayName("redirect 파라미터가 없어도 카카오 로그인 시작은 정상 동작한다")
  void startKakaoLogin_withoutRedirectParam_stillRedirectsToKakaoAuthorizationServer()
      throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorization/kakao"))
        .andExpect(status().is3xxRedirection())
        .andExpect(
            header().string("Location", startsWith("https://kauth.kakao.com/oauth/authorize")));
  }
}
