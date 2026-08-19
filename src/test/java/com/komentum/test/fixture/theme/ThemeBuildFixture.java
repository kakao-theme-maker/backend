package com.komentum.test.fixture.theme;

import com.komentum.global.security.UserRole;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.UUID;

public final class ThemeBuildFixture {

  private ThemeBuildFixture() {
  }

  public static User user(String email, UserRole role) {
    return User.builder()
        .publicUserId(UUID.randomUUID().toString())
        .userEmail(email)
        .name("theme build test user")
        .role(role)
        .build();
  }

  public static ThemeComponent theme(String ownerEmail) {
    return ThemeComponent.builder()
        .userEmail(ownerEmail)
        .themeName("theme build test theme")
        .versionNumber("1")
        .versionName("1.0.0")
        .isDone(true)
        .isPublic(false)
        .build();
  }
}
