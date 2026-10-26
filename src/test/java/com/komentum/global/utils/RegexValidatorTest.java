package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegexValidatorTest {

  private final String[] allowedOrigins = {
      "http://localhost:1234",
      "https://*.vercel.app"
  };

  @Test
  @DisplayName("origin이 허용 목록에 정확히 일치하면 true를 반환한다")
  void isAllowedOrigin_exactMatch_true() {
    assertThat(RegexValidator.isAllowedOrigin("http://localhost:1234", allowedOrigins)).isTrue();
  }

  @Test
  @DisplayName("origin이 와일드카드 패턴에 일치하면 true를 반환한다")
  void isAllowedOrigin_wildcardMatch_true() {
    assertThat(
        RegexValidator.isAllowedOrigin("https://my-app.vercel.app", allowedOrigins)).isTrue();
  }

  @Test
  @DisplayName("origin이 허용 목록에 없거나, host가 다르면 false를 반환한다")
  void isAllowedOrigin_notAllowed_false() {
    assertThat(RegexValidator.isAllowedOrigin("https://evil.com", allowedOrigins)).isFalse();
    assertThat(
        RegexValidator.isAllowedOrigin("https://localhost:3000", allowedOrigins)).isFalse();
  }

  @Test
  @DisplayName("origin이나 허용 목록이 null이면 false를 반환한다")
  void isAllowedOrigin_nullInputs_false() {
    assertThat(RegexValidator.isAllowedOrigin(null, allowedOrigins)).isFalse();
    assertThat(RegexValidator.isAllowedOrigin("http://localhost:3000", null)).isFalse();
  }
}
