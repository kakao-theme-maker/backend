package com.komentum.test.fixture.theme;

import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;

public final class ThemeBuildFixture {

  private ThemeBuildFixture() {
  }

  public static ThemeComponent theme(User owner) {
    return ThemeComponent.builder()
        .user(owner)
        .themeName("theme build test theme")
        .versionNumber("1")
        .versionName("1.0.0")
        .isDone(true)
        .isPublic(false)
        .build();
  }
}
