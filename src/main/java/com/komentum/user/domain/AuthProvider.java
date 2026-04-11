package com.komentum.user.domain;

import java.util.Arrays;

public enum AuthProvider {
  LOCAL("local"),
  KAKAO("kakao");

  private final String registrationId;

  AuthProvider(String registrationId) {
    this.registrationId = registrationId;
  }

  public static AuthProvider from(String registrationId) {
    return Arrays.stream(values())
        .filter(platform -> platform.registrationId.equals(registrationId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported oauth2 provider"));
  }
}