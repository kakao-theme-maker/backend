package com.komentum.service;

import com.komentum.config.UserTestDataGenerator;
import com.komentum.domain.User;
import com.komentum.dto.UserResponseDto;
import com.komentum.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test", "auth"})
class UserRetrieveServiceTest {

  @Autowired
  private UserRetrieveService userRetrieveService;

  @Autowired
  private UserTestDataGenerator userTestDataGenerator;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userTestDataGenerator.generateFakeData();
  }

  @AfterEach
  void tearDown() {
    userTestDataGenerator.removeFakeData();
  }

  @Test
  @DisplayName("사용자 조회 성공 테스트")
  void getUserByEmail_success() {
    // given
    String userEmail = userTestDataGenerator.userEmails.get(0);
    User user = userRepository.findById(userEmail).orElse(null);
    // when
    UserResponseDto res = userRetrieveService.getUserByEmail(userEmail);
    // then
    assertNotNull(user);
    assertNotNull(res);
    assert res.getUserEmail().equals(userEmail);
  }

  @Test
  @DisplayName("사용자 조회 실패 테스트")
  void getUserByEmail_failure() {
    // given
    String ghostUser = UUID.randomUUID().toString();
    // when + then
    assertThrows(RuntimeException.class, () -> userRetrieveService.getUserByEmail(ghostUser));
  }
}