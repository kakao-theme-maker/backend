package com.komentum.user.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport.UserScenarioResult;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import com.komentum.user.repository.FollowRepository;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class FollowControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private FollowRepository followRepository;

  private UserScenarioResult userScenarioResult;

  @BeforeEach
  void setUp() {
    userScenarioResult = userScenarioSupport.builder()
        .withUsers(3)
        .build();
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("사용자를 팔로우하고 중복 요청 시 기존 관계를 유지한다")
  void follow_successAndDuplicateMaintainsRelationship() throws Exception {
    User follower = userScenarioResult.users().get(0);
    User followee = userScenarioResult.users().get(1);

    follow(follower, followee.getPublicUserId(), 200);
    follow(follower, followee.getPublicUserId(), 200);

    assertThat(followRepository.existsByFollowerAndFollowee(follower, followee)).isTrue();
    assertThat(followRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("팔로우를 해제하고 관계가 없어도 해제 상태를 유지한다")
  void unfollow_successAndMissingRelationshipMaintainsState() throws Exception {
    User follower = userScenarioResult.users().get(0);
    User followee = userScenarioResult.users().get(1);
    follow(follower, followee.getPublicUserId(), 200);

    unfollow(follower, followee.getPublicUserId(), 204);
    unfollow(follower, followee.getPublicUserId(), 204);

    assertThat(followRepository.count()).isZero();
  }

  @Test
  @DisplayName("자기 자신은 팔로우할 수 없다")
  void follow_self_returnsBadRequest() throws Exception {
    User user = userScenarioResult.getFirstUser();

    follow(user, user.getPublicUserId(), 400);

    assertThat(followRepository.count()).isZero();
  }

  @Test
  @DisplayName("존재하지 않는 사용자는 팔로우하거나 팔로우 해제할 수 없다")
  void followAndUnfollow_missingUser_returnsNotFound() throws Exception {
    User follower = userScenarioResult.getFirstUser();
    String missingPublicUserId = UUID.randomUUID().toString();

    follow(follower, missingPublicUserId, 404);
    unfollow(follower, missingPublicUserId, 404);

    assertThat(followRepository.count()).isZero();
  }

  private void follow(User follower, String followeePublicUserId, int statusCode)
      throws Exception {
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/users/%s/follow", followeePublicUserId))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(follower))
            .statusCode(statusCode)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
  }

  private void unfollow(User follower, String followeePublicUserId, int statusCode)
      throws Exception {
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/users/%s/follow", followeePublicUserId))
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(follower))
            .statusCode(statusCode)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
  }
}
