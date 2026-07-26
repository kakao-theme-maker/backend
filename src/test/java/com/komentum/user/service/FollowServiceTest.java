package com.komentum.user.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.komentum.user.domain.Follow;
import com.komentum.user.domain.User;
import com.komentum.user.repository.FollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

  @Mock
  private FollowRepository followRepository;

  @Mock
  private UserEntityFinder userEntityFinder;

  @InjectMocks
  private FollowService followService;

  private User follower;
  private User followee;

  @BeforeEach
  void setUp() {
    follower = User.builder().userId(1L).publicUserId("follower").build();
    followee = User.builder().userId(2L).publicUserId("followee").build();
    given(userEntityFinder.findUserEntity("follower")).willReturn(follower);
    given(userEntityFinder.findUserEntity("followee")).willReturn(followee);
  }

  @Test
  @DisplayName("동시 중복 팔로우로 저장 충돌이 발생하면 기존 관계를 유지한다")
  void follow_concurrentDuplicateMaintainsRelationship() {
    given(followRepository.existsByFollowerAndFollowee(follower, followee))
        .willReturn(false)
        .willReturn(true);
    given(followRepository.saveAndFlush(any(Follow.class)))
        .willThrow(new DataIntegrityViolationException("duplicate follow"));

    assertThatCode(() -> followService.follow("follower", "followee"))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("중복 관계가 아닌 무결성 오류는 호출자에게 전달한다")
  void follow_nonDuplicateIntegrityViolationThrowsException() {
    given(followRepository.existsByFollowerAndFollowee(follower, followee))
        .willReturn(false);
    given(followRepository.saveAndFlush(any(Follow.class)))
        .willThrow(new DataIntegrityViolationException("invalid follow"));

    assertThatThrownBy(() -> followService.follow("follower", "followee"))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
