package com.komentum.theme.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImageInsetTest {

  @Test
  @DisplayName("inset은 가로, 세로 배율에 맞춰 각각 변환된다")
  void scale_convertByEachAxisRatio() {
    ImageInset inset = new ImageInset(300, 280, 30, 40, 30, 40);

    ImageInset scaled = inset.scale(600, 560, 122, 114);

    assertThat(scaled.getStretchX()).isEqualTo(61);
    assertThat(scaled.getStretchY()).isEqualTo(57);
    assertThat(scaled.getEdgeInsetLeft()).isEqualTo(8);
    assertThat(scaled.getEdgeInsetRight()).isEqualTo(8);
    assertThat(scaled.getEdgeInsetTop()).isEqualTo(6);
    assertThat(scaled.getEdgeInsetBottom()).isEqualTo(6);
    assertThat(scaled.isInImageRange(122, 114)).isTrue();
  }

  @Test
  @DisplayName("원본에 있던 콘텐츠 영역이 반올림으로 사라지면 끝쪽 inset을 줄여 최소 1px을 남긴다")
  void scale_keepContentAreaAtLeastOnePixel() {
    ImageInset inset = new ImageInset(50, 50, 50, 50, 49, 49);

    ImageInset scaled = inset.scale(100, 100, 10, 10);

    assertThat(scaled.getEdgeInsetLeft()).isEqualTo(5);
    assertThat(scaled.getEdgeInsetRight()).isEqualTo(4);
    assertThat(scaled.getEdgeInsetTop()).isEqualTo(5);
    assertThat(scaled.getEdgeInsetBottom()).isEqualTo(4);
  }

  @Test
  @DisplayName("변환은 새 inset을 반환하고, DB에 저장된 원본 inset은 수정하지 않는다")
  void scale_doesNotModifyOriginal() {
    ImageInset inset = new ImageInset(300, 280, 30, 40, 30, 40);

    ImageInset scaled = inset.scale(600, 560, 122, 114);

    assertThat(scaled).isNotSameAs(inset);
    assertThat(inset.getStretchX()).isEqualTo(300);
    assertThat(inset.getEdgeInsetLeft()).isEqualTo(40);
  }

  @Test
  @DisplayName("inset 값이 하나라도 없으면 변환할 수 없다")
  void scale_failWhenValueMissing() {
    ImageInset inset = new ImageInset(300, null, 30, 40, 30, 40);

    assertThatThrownBy(() -> inset.scale(600, 560, 122, 114))
        .isInstanceOf(IllegalStateException.class);
  }
}
