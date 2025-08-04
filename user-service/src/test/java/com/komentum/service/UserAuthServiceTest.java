package com.komentum.service;

import com.komentum.auth.JwtUtils;
import com.komentum.client.KakaoAuthHttpClient;
import com.komentum.domain.Gender;
import com.komentum.domain.User;
import com.komentum.dto.KakaoUserInfo;
import com.komentum.dto.UserAuthResponse;
import com.komentum.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles({"test", "auth"})
class UserAuthServiceTest {

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private JwtUtils jwtUtils;

  @MockitoBean
  private KakaoAuthHttpClient kakaoAuthHttpClient;

  @MockitoBean
  private TokenService tokenService;
  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("카카오 로그인 성공 테스트")
  void processKakaoAuth_success() {
    // given
    User user = User.builder()
        .userEmail("test@test.com")
        .birth(LocalDate.now())
        .gender(Gender.male)
        .profileImg("profileImage").build();

    KakaoUserInfo kakaoUserInfo = KakaoUserInfo.builder()
        .email(user.getUserEmail())
        .birth(user.getBirth())
        .gender(user.getGender().toString())
        .profileImage(user.getProfileImg()).build();

    // stub
    when(kakaoAuthHttpClient.processLogin(anyString()))
        .thenReturn(Mono.just(kakaoUserInfo));
    when(tokenService.saveAccessAndRefreshToken(any(), any(), any())).thenReturn(true);

    // when
    UserAuthResponse res = userAuthService.processKakaoAuth("authCode").block();

    // then
    assertNotNull(res);
    assert jwtUtils.validateToken(res.getAccessToken());
    assert jwtUtils.validateToken(res.getAccessToken());
    assert jwtUtils.getEmail(res.getAccessToken()).equals(user.getUserEmail());
  }

  @Test
  @DisplayName("로그아웃 성공 테스트")
  void handleLogout_success() {
    // given
    User user = User.builder()
        .userEmail("test@test.com")
        .birth(LocalDate.now())
        .gender(Gender.male)
        .profileImg("profileImage").build();
    user = userRepository.save(user);
    String accessToken = jwtUtils.generateAccessToken(user.getUserEmail());

    // stub
    when(tokenService.deleteAccessToken(user.getUserEmail())).thenReturn(true);
    when(tokenService.deleteRefreshToken(user.getUserEmail())).thenReturn(true);

    // when
    assertDoesNotThrow(() -> {
      userAuthService.handleLogout(accessToken);
    });
  }

  @Test
  @DisplayName("토큰 재발급 성공 테스트")
  void validateRefreshToken_success() throws InterruptedException {
    // given
    User user = User.builder()
        .userEmail("test@test.com")
        .birth(LocalDate.now())
        .gender(Gender.male)
        .profileImg("profileImage").build();
    user = userRepository.save(user);
    String refreshToken = jwtUtils.generateRefreshToken(user.getUserEmail());

    // stub
    when(tokenService.getRefreshToken(user.getUserEmail())).thenReturn(refreshToken);
    when(tokenService.saveAccessAndRefreshToken(any(), any(), any())).thenReturn(true);

    // when
    Thread.sleep(1000L); // 1초 sleep 하여 새로 생성되는 토큰이 기존 토큰과 다르게 만든다.
    UserAuthResponse res = userAuthService.doRefreshTokenRotation(refreshToken);

    // then
    assertNotNull(res);
    assert !res.getRefreshToken().equals(refreshToken);
    assert jwtUtils.validateToken(res.getAccessToken());
    assert jwtUtils.validateToken(res.getRefreshToken());
  }
}