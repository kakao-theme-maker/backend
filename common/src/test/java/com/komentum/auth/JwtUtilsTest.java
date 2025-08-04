package com.komentum.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

  @Mock
  private JwtUtils jwtUtils;

  String secretKey = "TestSecretKey1234TestSecretKey1234TestSecretKey1234TestSecretKey1234TestSecretKey1234";

  @BeforeEach
  void setUp() {
    jwtUtils = new JwtUtils(secretKey);
  }

  @Test
  void validateToken_test() {
    // given
    String userEmail = "test@test.com";
    String accessToken = jwtUtils.generateAccessToken(userEmail);
    String wrongToken = "wrongToken";
    // when + then
    assert jwtUtils.validateToken(accessToken);
    assert !jwtUtils.validateToken(wrongToken);
  }

  @Test
  void generateAccessToken_test() {
    // given
    String userEmail = "test@test.com";
    // when
    String accessToken = jwtUtils.generateAccessToken(userEmail);
    // then
    assertNotNull(accessToken);
  }

  @Test
  void generateRefreshToken_test() {
    // given
    String userEmail = "test@test.com";
    // when
    String refreshToken = jwtUtils.generateRefreshToken(userEmail);
    // then
    assertNotNull(refreshToken);
  }

  @Test
  void getEmail_test() {
    // given
    String userEmail = "test@test.com";
    String ghostEmail = "ghost@test.com";
    String accessToken = jwtUtils.generateAccessToken(userEmail);
    String wrongToken = "wrongToken";
    // when
    String savedEmail = jwtUtils.getEmail(accessToken);
    String mustBeNull = jwtUtils.getEmail(wrongToken);
    // then
    assert !savedEmail.equals(ghostEmail);
    assert savedEmail.equals(userEmail);
    assert mustBeNull == null;
  }
}