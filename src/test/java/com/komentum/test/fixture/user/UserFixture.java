package com.komentum.test.fixture.user;

import com.komentum.global.security.UserRole;
import com.komentum.user.domain.User;
import java.util.UUID;

public final class UserFixture {

  private UserFixture() {
  }

  public static User user(String email, UserRole role) {
    return User.builder()
        .publicUserId(UUID.randomUUID().toString())
        .userEmail(email)
        .name("theme build test user")
        .role(role)
        .build();
  }
}
