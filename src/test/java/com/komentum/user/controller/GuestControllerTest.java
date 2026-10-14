package com.komentum.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.komentum.global.properties.AuthProperty;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class GuestControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MockMvcUtils mockMvcUtils;
  @Autowired
  private AuthProperty authProperty;

  @Test
  @DisplayName("게스트 토큰 쿠키의 만료 시간은 access token이 아닌 게스트 토큰 만료 시간을 따른다")
  void issueGuestToken_cookieMaxAgeIsGuestTokenExpiresIn() throws Exception {
    // when
    ResultActions result = mockMvcUtils.performRequest(
        MockMvcRequestBuilders.post("/api/guest-tokens"),
        ExecutionContext.builder().mockMvc(mockMvc).build());
    // then : 응답 200 확인
    result.andExpect(status().isOk());
    // then : cookie의 토큰이 게스트 토큰이 맞는지 확인
    Cookie guestCookie = result.andReturn().getResponse()
        .getCookie(AuthProperty.ACCESS_TOKEN_COOKIE_NAME);
    assertThat(guestCookie).isNotNull();
    assertThat(guestCookie.getMaxAge())
        .isEqualTo(authProperty.getGuestTokenExpiresIn().intValue());
  }
}
