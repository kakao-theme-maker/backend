package com.komentum.designcomponent.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.komentum.theme.utils.ColorEditor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ColorEditorTest {

  @Test
  @DisplayName("원본 색상의 RGB에 가중치를 곱해서 반환한다")
  public void toDarkColor_success() {
    // given
    String hexColor = "#FFFFFF";
    String expected = "#E5E5E5";
    double weight = 0.9;
    // when
    String actual = ColorEditor.toDarkColor(hexColor, weight);
    // then
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @DisplayName("유효하지 않은 hex color의 경우, 예외를 던진다")
  public void toDarkColor_invalidHexColor() {
    // given
    String invalid1 = "helloWorld";
    // when + then
    assertThrows(IllegalArgumentException.class,
        () -> ColorEditor.toDarkColor(invalid1, 0.9));
  }
}