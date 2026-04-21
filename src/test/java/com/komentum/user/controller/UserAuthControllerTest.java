package com.komentum.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.komentum.auth.JwtUtils;
import com.komentum.global.properties.AuthProperty;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.user.domain.User;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
class UserAuthControllerTest {

  String email = "admin1@gmail.com";
  String password = "qwer123!";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Autowired
  private JwtUtils jwtUtils;
  @Autowired
  private BCryptPasswordEncoder passwordEncoder;
  @Autowired
  private MockMvcUtils mockMvcUtils;

  User user;

  User testClient;

  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    userDataGenerator.generateTestLocalUser(email, password);
    user = userRepository.findByUserEmail(email).orElseThrow();
  }

  @AfterEach
  void tearDown() {
    userDataGenerator.deleteAllUsers();
  }

  @Autowired
  private UserAuthController userAuthController;

  @Test
  @DisplayName("회원가입")
  void signUpWithLocalTest() throws Exception {
    //given
    String signUpEmail = "admin1@gmail.com";
    String signUpPassword = "qwer123!";
    LocalLoginRequestDto localLoginRequestDto
        = LocalLoginRequestDto.builder()
        .email(signUpEmail)
        .password(signUpPassword)
        .build();
    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/local/sign-up")
        .content(objectMapper.writeValueAsString(localLoginRequestDto))
        .contentType(MediaType.APPLICATION_JSON); //웬만하면 하는 게 좋음
    //then
    mockMvc.perform(request)
        .andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();
    assertThat(userRepository.findByUserEmail(email)).isPresent();
  }

  @Test
  @DisplayName("로그인 jwt 토큰 검증")
  void signInWithLocalTest() throws Exception {
    //given
    LocalLoginRequestDto localLoginRequestDto =
        LocalLoginRequestDto.builder()
            .email(email)
            .password(password)
            .build();
    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/local/sign-in")
        .content(objectMapper.writeValueAsString(localLoginRequestDto))
        .contentType(MediaType.APPLICATION_JSON);

    //then
    UserAuthResponse userAuthResponse = objectMapper.readValue((mockMvc.perform(request)
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString()), UserAuthResponse.class);

    //jwt token이 정상적인가
    assertThat(jwtUtils.validateToken(userAuthResponse.getAccessToken())).isTrue();

  }

  @Test
  @DisplayName("비밀번호 변경")
  void changePassword() throws Exception {
    //given
    String userEmail = "admin1@gmail.com";
    String newPassword = "123qwer!";

    PasswordChangeRequsetDto requsetDto = PasswordChangeRequsetDto.builder().
        currentPassword(password)
        .newPassword(newPassword)
        .build();

    String token = jwtUtils.generateAccessToken(user.getPublicUserId());

    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.patch("/api/users/me/password")
        .content(objectMapper.writeValueAsString(requsetDto))
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "Bearer " + token);

    //then
    mockMvc.perform(request)
        .andExpect(status().is2xxSuccessful());

    User updatedUSer = userRepository.findByUserEmail(userEmail).orElseThrow();

    assertThat(updatedUSer.matchPassword(newPassword, passwordEncoder)).isTrue();
  }

  @Test
  @DisplayName("when send request, rotate refresh token and return new tokens")
  public void doRefreshTokenRotation_success() throws Exception {
    // given: 사용자 1명 로그인 처리
    LocalLoginRequestDto localLoginRequestDto = new LocalLoginRequestDto(email, password);
    String response = mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/local/sign-in")
            .content(objectMapper.writeValueAsString(localLoginRequestDto))
            .contentType(MediaType.APPLICATION_JSON)
    ).andReturn().getResponse().getContentAsString();
    UserAuthResponse tokenResponse = objectMapper.readValue(response, UserAuthResponse.class);
    // when: 토큰 재발급 시도
    Thread.sleep(1000);
    String newResponse = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/reissue")
                .cookie(
                    new Cookie(AuthProperty.REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken())
                )
        )
        .andExpect(header().stringValues(
            HttpHeaders.SET_COOKIE,
            hasItem(containsString(AuthProperty.ACCESS_TOKEN_COOKIE_NAME))
        ))
        .andExpect(header().stringValues(
            HttpHeaders.SET_COOKIE,
            hasItem(containsString(AuthProperty.REFRESH_TOKEN_COOKIE_NAME))
        ))
        .andReturn().getResponse().getContentAsString();
    UserAuthResponse newTokenResponse = objectMapper.readValue(newResponse, UserAuthResponse.class);
    // then: 토큰 유효성 확인
    assertThat(jwtUtils.isAccessToken(newTokenResponse.getAccessToken())).isTrue();
    assertThat(jwtUtils.validateToken(newTokenResponse.getAccessToken())).isTrue();
    assertThat(jwtUtils.isRefreshToken(newTokenResponse.getRefreshToken())).isTrue();
    assertThat(jwtUtils.validateToken(newTokenResponse.getRefreshToken())).isTrue();
    // then: 새로운 토큰이 발급되었는지 확인
    assertThat(newTokenResponse.getAccessToken()).isNotEqualTo(tokenResponse.getAccessToken());
    assertThat(newTokenResponse.getRefreshToken()).isNotEqualTo(tokenResponse.getRefreshToken());
  }

  @Test
  @DisplayName("when send request with used refresh token, throw 401 exception")
  public void doRefreshTokenRotation_withUsedRefreshToken() throws Exception {
    // given: 사용자 1명 로그인 처리
    LocalLoginRequestDto localLoginRequestDto = new LocalLoginRequestDto(email, password);
    String response = mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/local/sign-in")
            .content(objectMapper.writeValueAsString(localLoginRequestDto))
            .contentType(MediaType.APPLICATION_JSON)
    ).andReturn().getResponse().getContentAsString();
    UserAuthResponse tokenResponse = objectMapper.readValue(response, UserAuthResponse.class);
    // when: 토큰 재발급 시도
    Thread.sleep(1000);
    mockMvc.perform(
        MockMvcRequestBuilders.post("/api/auth/reissue")
            .cookie(
                new Cookie(AuthProperty.REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken()))
    );
    // when: 이전에 사용한 refresh token 재사용
    mockMvc.perform(
            MockMvcRequestBuilders.post("/api/auth/reissue")
                .cookie(
                    new Cookie(AuthProperty.REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken()))
        )
        .andExpect(status().isUnauthorized());
  }

}