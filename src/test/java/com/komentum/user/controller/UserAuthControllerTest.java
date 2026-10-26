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
import com.komentum.global.security.UserRole;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.PasswordChangeRequsetDto;
import com.komentum.user.dto.SignUpRequestDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
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
  @DisplayName("인증 정보 없이 회원가입하면, 새로운 일반 사용자가 생성된다")
  void signUpWithLocalTest() throws Exception {
    //given
    String signUpEmail = "newuser@gmail.com";
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
    User signedUpUser = userRepository.findByUserEmail(signUpEmail).orElseThrow();
    assertThat(signedUpUser.getRole()).isEqualTo(UserRole.USER);
  }

  @Test
  @DisplayName("이미 존재하는 이메일로 회원가입하면, 409 응답을 반환한다")
  void signUpWithDuplicatedEmail() throws Exception {
    //given
    SignUpRequestDto signUpRequestDto = new SignUpRequestDto(email, password);
    long userCount = userRepository.count();
    //when & then
    mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/local/sign-up")
            .content(objectMapper.writeValueAsString(signUpRequestDto))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
    assertThat(userRepository.count()).isEqualTo(userCount);
  }

  @Test
  @DisplayName("게스트 인증 정보로 회원가입하면, 기존 게스트 사용자가 동일한 ID로 일반 사용자로 승격된다")
  void signUpWithGuestToken_promoteGuest() throws Exception {
    //given
    User guest = userRepository.save(User.createGuest(UUID.randomUUID().toString()));
    String guestToken = jwtUtils.generateAccessToken(guest.getPublicUserId());
    String signUpEmail = "guest-promoted@gmail.com";
    SignUpRequestDto signUpRequestDto = new SignUpRequestDto(signUpEmail, "qwer123!");
    long userCount = userRepository.count();
    //when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/api/auth/local/sign-up")
        .content(objectMapper.writeValueAsString(signUpRequestDto))
        .contentType(MediaType.APPLICATION_JSON);
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        TestClientDto.fromEntity(guest));
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
    //then
    User promoted = userRepository.findByUserEmail(signUpEmail).orElseThrow();
    assertThat(promoted.getUserId()).isEqualTo(guest.getUserId());
    assertThat(promoted.getPublicUserId()).isEqualTo(guest.getPublicUserId());
    assertThat(promoted.getRole()).isEqualTo(UserRole.USER);
    assertThat(promoted.matchPassword("qwer123!", passwordEncoder)).isTrue();
    assertThat(userRepository.count()).isEqualTo(userCount);
  }

  @Test
  @DisplayName("게스트가 아닌 사용자의 인증 정보로 회원가입하면, 예외가 발생하고 권한은 변경되지 않는다")
  void signUpWithUserToken_fail() throws Exception {
    //given
    String signUpEmail = "another@gmail.com";
    SignUpRequestDto signUpRequestDto = new SignUpRequestDto(signUpEmail, "qwer123!");
    //when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
        .post("/api/auth/local/sign-up")
        .content(objectMapper.writeValueAsString(signUpRequestDto))
        .contentType(MediaType.APPLICATION_JSON);
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        TestClientDto.fromEntity(user));
    mockMvc.perform(requestBuilder)
        .andExpect(status().is5xxServerError());
    //then
    User unchanged = userRepository.findByPublicUserId(user.getPublicUserId()).orElseThrow();
    assertThat(unchanged.getRole()).isEqualTo(UserRole.USER);
    assertThat(unchanged.getUserEmail()).isEqualTo(email);
    assertThat(userRepository.findByUserEmail(signUpEmail)).isEmpty();
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