package com.komentum.global.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "test-user")
public class TestUserProperty {

  private final String userEmail;
  private final String password;
  private final String publicUserId;
}
