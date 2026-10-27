package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.OAuth2UserInfo;
import com.komentum.global.properties.AuthProperty;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.user.domain.AuthProvider;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

@SpringBootTest
@EnableTestProfile
class CustomOauth2UserServiceIntegrationTest {

  @Autowired
  private CustomOauth2UserService customOauth2UserService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserDataGenerator userDataGenerator;
  @Autowired
  private JwtUtils jwtUtils;

  private final String kakaoEmail = "kakao-user@kakao.com";
  private final OAuth2UserInfo kakaoUserInfo = OAuth2UserInfo.builder()
      .userEmail(kakaoEmail)
      .name("kakaoUser")
      .profileImageUrl("https://kakao/profile.png")
      .authProvider(AuthProvider.KAKAO)
      .build();

  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
  }

  @AfterEach
  void tearDown() {
    userDataGenerator.deleteAllUsers();
  }

  /**
   * 카카오 콜백 요청처럼 access token 쿠키를 가진 요청으로 소셜 로그인을 처리한다
   */
  private User loginWithKakao(String accessToken) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    if (accessToken != null) {
      request.setCookies(new Cookie(AuthProperty.ACCESS_TOKEN_COOKIE_NAME, accessToken));
    }
    String guestPublicUserId = customOauth2UserService.resolveGuestPublicUserId(request);
    return customOauth2UserService.createOrRetrieveUser(kakaoUserInfo, guestPublicUserId);
  }

  @Test
  @DisplayName("유효한 게스트 토큰으로 카카오 로그인하면, 기존 게스트 사용자가 동일한 ID로 승격되고 카카오 정보가 반영된다")
  void kakaoLogin_withGuestToken_promoteGuest() {
    // given
    User guest = userRepository.save(User.createGuest(UUID.randomUUID().toString()));
    String guestToken = jwtUtils.generateAccessToken(guest.getPublicUserId());
    long userCount = userRepository.count();
    // when
    loginWithKakao(guestToken);
    // then
    User promoted = userRepository.findByPublicUserId(guest.getPublicUserId()).orElseThrow();
    assertThat(promoted.getUserId()).isEqualTo(guest.getUserId());
    assertThat(promoted.getRole()).isEqualTo(UserRole.USER);
    assertThat(promoted.getUserEmail()).isEqualTo(kakaoEmail);
    assertThat(promoted.getName()).isEqualTo("kakaoUser");
    assertThat(promoted.getProfileImgUrl()).isEqualTo("https://kakao/profile.png");
    assertThat(promoted.getAuthProvider()).isEqualTo(AuthProvider.KAKAO);
    assertThat(userRepository.count()).isEqualTo(userCount);
  }

  @Test
  @DisplayName("게스트 토큰 없이 카카오 로그인하면, 새로운 일반 사용자가 생성된다")
  void kakaoLogin_withoutToken_createNewUser() {
    // when
    User user = loginWithKakao(null);
    // then
    User saved = userRepository.findByUserEmail(kakaoEmail).orElseThrow();
    assertThat(saved.getUserId()).isEqualTo(user.getUserId());
    assertThat(saved.getRole()).isEqualTo(UserRole.USER);
    assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.KAKAO);
  }

  @Test
  @DisplayName("유효하지 않은 토큰으로 카카오 로그인하면, 해당 토큰을 무시하고 새로운 사용자를 생성한다")
  void kakaoLogin_withInvalidToken_createNewUser() {
    // given
    User guest = userRepository.save(User.createGuest(UUID.randomUUID().toString()));
    // when
    User user = loginWithKakao("invalid.token.value");
    // then
    assertThat(user.getUserId()).isNotEqualTo(guest.getUserId());
    assertThat(userRepository.findByPublicUserId(guest.getPublicUserId()).orElseThrow().getRole())
        .isEqualTo(UserRole.GUEST);
  }

  @Test
  @DisplayName("이미 카카오로 가입한 회원이라면 기존 사용자 정보를 사용하고, 게스트 정보는 무시한다")
  void kakaoLogin_existingMemberWithGuestToken_loginExistingMember() {
    // given
    User member = userRepository.save(kakaoUserInfo.toEntity());
    User guest = userRepository.save(User.createGuest(UUID.randomUUID().toString()));
    String guestToken = jwtUtils.generateAccessToken(guest.getPublicUserId());
    // when
    User user = loginWithKakao(guestToken);
    // then
    assertThat(user.getUserId()).isEqualTo(member.getUserId());
    assertThat(userRepository.findByPublicUserId(guest.getPublicUserId()).orElseThrow().getRole())
        .isEqualTo(UserRole.GUEST);
  }
}
