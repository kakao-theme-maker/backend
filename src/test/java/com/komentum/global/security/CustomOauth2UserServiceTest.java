package com.komentum.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.komentum.global.dto.OAuth2UserInfo;
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
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo);
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
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo);
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
    User res = customOauth2UserService.createOrRetrieveUser(oauth2UserInfo);
    // then
    assertThat(res).isEqualTo(user);
    verify(userRepository, times(2)).findByUserEmail(userEmail);
    verify(userRepository, times(1)).save(user);
  }
}