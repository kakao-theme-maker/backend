package com.komentum.theme.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeStyleTest {

  @Test
  @DisplayName("color와 alpha가 모두 전달되면 기존 값을 덮어쓴다")
  void updateColorAndAlpha_overwritesExistingValues() {
    // given
    ThemeStyle themeStyle = ThemeStyle.builder()
        .color("#000000")
        .alpha(10)
        .build();
    // when
    themeStyle.updateColorAndAlpha("#FFFFFF", 80);
    // then
    assertThat(themeStyle.getColor()).isEqualTo("#FFFFFF");
    assertThat(themeStyle.getAlpha()).isEqualTo(80);
  }

  @Test
  @DisplayName("color가 null이면 예외가 발생한다")
  void updateColorAndAlpha_nullColor_throws() {
    // given
    ThemeStyle themeStyle = ThemeStyle.builder().color("#000000").alpha(10).build();
    // when & then
    assertThatThrownBy(() -> themeStyle.updateColorAndAlpha(null, 80))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("alpha가 null이면 예외가 발생한다")
  void updateColorAndAlpha_nullAlpha_throws() {
    // given
    ThemeStyle themeStyle = ThemeStyle.builder().color("#000000").alpha(10).build();
    // when & then
    assertThatThrownBy(() -> themeStyle.updateColorAndAlpha("#FFFFFF", null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("alpha가 0~100 범위를 벗어나면 예외가 발생한다")
  void updateColorAndAlpha_alphaOutOfRange_throws() {
    // given
    ThemeStyle themeStyle = ThemeStyle.builder().color("#000000").alpha(10).build();
    // when & then
    assertThatThrownBy(() -> themeStyle.updateColorAndAlpha("#FFFFFF", 101))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> themeStyle.updateColorAndAlpha("#FFFFFF", -1))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("iOS용 RGBA 변환 시 alpha 백분율을 16진수로 변환해 color 뒤에 붙인다")
  void getColorWithAlphaRgba_appendsHexAlphaAfterColor() {
    // given
    ThemeStyle fullyOpaque = ThemeStyle.builder().color("#FFFFFF").alpha(100).build();
    ThemeStyle fullyTransparent = ThemeStyle.builder().color("#FFFFFF").alpha(0).build();
    ThemeStyle halfAlpha = ThemeStyle.builder().color("#112233").alpha(50).build();
    // when & then
    assertThat(fullyOpaque.getColorWithAlphaRgba()).isEqualTo("#FFFFFFFF");
    assertThat(fullyTransparent.getColorWithAlphaRgba()).isEqualTo("#FFFFFF00");
    assertThat(halfAlpha.getColorWithAlphaRgba()).isEqualTo("#11223380");
  }

  @Test
  @DisplayName("Android용 ARGB 변환 시 alpha 백분율을 16진수로 변환해 color 앞에 붙인다")
  void getColorWithAlphaArgb_prependsHexAlphaBeforeColor() {
    // given
    ThemeStyle fullyOpaque = ThemeStyle.builder().color("#FFFFFF").alpha(100).build();
    ThemeStyle fullyTransparent = ThemeStyle.builder().color("#FFFFFF").alpha(0).build();
    ThemeStyle halfAlpha = ThemeStyle.builder().color("#112233").alpha(50).build();
    // when & then
    assertThat(fullyOpaque.getColorWithAlphaArgb()).isEqualTo("#FFFFFFFF");
    assertThat(fullyTransparent.getColorWithAlphaArgb()).isEqualTo("#00FFFFFF");
    assertThat(halfAlpha.getColorWithAlphaArgb()).isEqualTo("#80112233");
  }

  @Test
  @DisplayName("color가 null/공백이거나 유효한 hex 형식이 아니면 변환 결과는 null이다")
  void getColorWithAlpha_invalidColor_returnsNull() {
    // given
    ThemeStyle blankColor = ThemeStyle.builder().color(" ").alpha(80).build();
    ThemeStyle malformedColor = ThemeStyle.builder().color("plum").alpha(80).build();
    // when & then
    assertThat(blankColor.getColorWithAlphaRgba()).isNull();
    assertThat(blankColor.getColorWithAlphaArgb()).isNull();
    assertThat(malformedColor.getColorWithAlphaRgba()).isNull();
    assertThat(malformedColor.getColorWithAlphaArgb()).isNull();
  }

  @Test
  @DisplayName("빌더로 alpha를 지정하지 않으면 기본값 100(불투명)이 적용된다")
  void builderDefault_alphaIsFullyOpaque() {
    // when
    ThemeStyle themeStyle = ThemeStyle.builder().color("#FFFFFF").build();
    // then
    assertThat(themeStyle.getAlpha()).isEqualTo(100);
  }
}
