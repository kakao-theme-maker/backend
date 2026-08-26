package com.komentum.test.fixture.theme;

import com.komentum.theme.core.domain.ThemeComponent;

public final class ThemeBuildFixture {

  private ThemeBuildFixture() {
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
