package com.komentum.designcomponent.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PlatformScopeTest {

  @Test
  @DisplayName("COMMON은 Android/iOS 플랫폼 모두를 지원한다")
  void common_supportsAllPlatforms() {
    assertThat(PlatformScope.COMMON.supports(Platform.ANDROID)).isTrue();
    assertThat(PlatformScope.COMMON.supports(Platform.IOS)).isTrue();
  }

  @Test
  @DisplayName("ANDROID는 Android 플랫폼만 지원한다")
  void android_supportsOnlyAndroid() {
    assertThat(PlatformScope.ANDROID.supports(Platform.ANDROID)).isTrue();
    assertThat(PlatformScope.ANDROID.supports(Platform.IOS)).isFalse();
  }

  @Test
  @DisplayName("IOS는 iOS 플랫폼만 지원한다")
  void ios_supportsOnlyIos() {
    assertThat(PlatformScope.IOS.supports(Platform.IOS)).isTrue();
    assertThat(PlatformScope.IOS.supports(Platform.ANDROID)).isFalse();
  }
}
