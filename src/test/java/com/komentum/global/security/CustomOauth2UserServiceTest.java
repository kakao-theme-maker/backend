package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.komentum.global.dto.OAuth2UserInfo;
import com.komentum.user.domain.AuthProvider;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CustomOauth2UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private OAuth2UserInfo oauth2UserInfo;

  @InjectMocks
  private CustomOauth2UserService customOauth2UserService;

  String userEmail = "testUser@test.com";

  @BeforeEach
  void setUp() {
    given(oauth2UserInfo.getUserEmail())
        .willReturn(userEmail);
  }

  @Test
  @DisplayName("if user exists in DB, return that user")
  void createOrRetrieveUser_whenUserExists() {
    // given
    User user = User.builder().build();
    given(userRepository.findByUserEmail(userEmail))
        .willReturn(Optional.of(user));
    // when
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo, null);
    // then
    assertThat(res).isEqualTo(user);
    verify(userRepository, times(1)).findByUserEmail(userEmail);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("if user not exists in DB, save user and return saved user")
  void createOrRetrieveUser_whenUserNotExists() {
    // given
    User user = User.builder().build();
    given(oauth2UserInfo.toEntity())
        .willReturn(user);
    given(userRepository.findByUserEmail(userEmail))
        .willReturn(Optional.empty());
    given(userRepository.save(user))
        .willReturn(user);
    // when
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo, null);
    // then
    assertThat(res).isEqualTo(user);
    verify(userRepository, times(1)).findByUserEmail(userEmail);
    verify(userRepository, times(1)).save(user);
  }

  @Test
  @DisplayName("if concurrency issue occurs, return user in DB")
  void createOrRetrieveUser_whenConcurrencyIssueOccurs() {
    // given
    User user = User.builder().build();
    given(oauth2UserInfo.toEntity())
        .willReturn(user);
    given(userRepository.findByUserEmail(userEmail))
        .willReturn(Optional.empty())
        .willReturn(Optional.of(user));
    given(userRepository.save(user))
        .willThrow(new DataIntegrityViolationException("exception"));
    // when
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo, null);
    // then
    assertThat(res).isEqualTo(user);
    verify(userRepository, times(2)).findByUserEmail(userEmail);
    verify(userRepository, times(1)).save(user);
  }

  @Test
  @DisplayName("게스트 사용자 ID가 주어지고 DB의 사용자가 GUEST라면, 새로 생성하지 않고 기존 사용자를 승격한다")
  void createOrRetrieveUser_whenGuestExists_promoteGuest() {
    // given
    User guest = User.createGuest("guest-public-id");
    given(oauth2UserInfo.getName()).willReturn("kakaoUser");
    given(oauth2UserInfo.getProfileImageUrl()).willReturn("https://profile");
    given(oauth2UserInfo.getAuthProvider()).willReturn(AuthProvider.KAKAO);
    given(userRepository.findByUserEmail(userEmail)).willReturn(Optional.empty());
    given(userRepository.findByPublicUserId("guest-public-id")).willReturn(Optional.of(guest));
    // when
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo, "guest-public-id");
    // then
    assertThat(res).isSameAs(guest);
    assertThat(res.getPublicUserId()).isEqualTo("guest-public-id");
    assertThat(res.getRole()).isEqualTo(UserRole.USER);
    assertThat(res.getUserEmail()).isEqualTo(userEmail);
    assertThat(res.getAuthProvider()).isEqualTo(AuthProvider.KAKAO);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("카카오 이메일로 가입된 사용자가 있으면, 게스트 사용자 ID를 무시하고 기존 사용자를 반환한다")
  void createOrRetrieveUser_whenUserExistsWithGuest_returnExistingUser() {
    // given
    User user = User.builder().role(UserRole.USER).build();
    given(userRepository.findByUserEmail(userEmail)).willReturn(Optional.of(user));
    // when
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo, "guest-public-id");
    // then
    assertThat(res).isEqualTo(user);
    verify(userRepository, never()).findByPublicUserId(any());
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("토큰의 사용자가 GUEST가 아니고 가입된 사용자도 아니라면, 새로운 사용자를 생성한다")
  void createOrRetrieveUser_whenTokenUserIsNotGuest_createNewUser() {
    // given
    User otherUser = User.builder().publicUserId("user-public-id").role(UserRole.USER).build();
    User newUser = User.builder().build();
    given(oauth2UserInfo.toEntity()).willReturn(newUser);
    given(userRepository.findByUserEmail(userEmail)).willReturn(Optional.empty());
    given(userRepository.findByPublicUserId("user-public-id")).willReturn(Optional.of(otherUser));
    given(userRepository.save(newUser)).willReturn(newUser);
    // when
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo, "user-public-id");
    // then
    assertThat(res).isEqualTo(newUser);
    assertThat(otherUser.getRole()).isEqualTo(UserRole.USER);
    verify(userRepository, times(1)).save(newUser);
  }
}
