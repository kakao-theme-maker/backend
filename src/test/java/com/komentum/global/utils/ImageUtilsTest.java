package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.enums.FileExtension;
import com.komentum.global.utils.ImageUtils.ImageSize;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;

class ImageUtilsTest {

  @Test
  @DisplayName("PNG 파일은 실제 포맷 기준으로 FileExtension.PNG로 판별한다")
  void detectImageExtension_success() throws IOException {
    try (InputStream png = new ClassPathResource("sample-images/test.png").getInputStream()) {
      assertThat(ImageUtils.detectImageExtension(png)).contains(FileExtension.PNG);
    }
  }

  @Test
  @DisplayName("JPEG처럼 FileExtension에 없는 이미지 포맷은 판별 결과가 비어 있다")
  void detectImageExtension_emptyWhenUnsupportedFormat() throws IOException {
    try (InputStream jpg = new ClassPathResource("sample-images/test.jpg").getInputStream()) {
      assertThat(ImageUtils.detectImageExtension(jpg)).isEmpty();
    }
  }

  @Test
  @DisplayName("이미지가 아닌 데이터는 확장자와 관계없이 판별 결과가 비어 있다")
  void detectImageExtension_emptyWhenNotImage() throws IOException {
    InputStream text = new ByteArrayInputStream("not an image".getBytes(StandardCharsets.UTF_8));
    assertThat(ImageUtils.detectImageExtension(text)).isEmpty();
    assertThat(ImageUtils.detectImageExtension(new ByteArrayInputStream(new byte[0]))).isEmpty();
  }

  @Test
  @DisplayName("PNG 이미지는 다시 인코딩하지 않고 원본 바이트를 그대로 반환한다")
  void toPng_returnOriginalWhenPng() throws IOException {
    byte[] png = new ClassPathResource("sample-images/test.png").getContentAsByteArray();
    assertThat(ImageUtils.toPng(png)).hasValueSatisfying(result -> assertThat(result).isSameAs(png));
  }

  @ParameterizedTest
  @ValueSource(strings = {"jpeg", "gif", "bmp", "tiff"})
  @DisplayName("JDK ImageIO 기본 포맷 이미지는 같은 크기의 PNG로 변환된다")
  void toPng_convertImageIoFormat(String formatName) throws IOException {
    byte[] source = writeImage(new BufferedImage(30, 20, BufferedImage.TYPE_INT_RGB), formatName);
    byte[] png = ImageUtils.toPng(source).orElseThrow();
    assertThat(ImageUtils.detectImageExtension(new ByteArrayInputStream(png)))
        .contains(FileExtension.PNG);
    assertThat(ImageUtils.readImageSize(new ByteArrayInputStream(png)))
        .contains(new ImageSize(30, 20));
  }

  @Test
  @DisplayName("이미지가 아니거나 비어 있는 데이터는 PNG로 변환할 수 없다")
  void toPng_emptyWhenNotImage() throws IOException {
    assertThat(ImageUtils.toPng("not an image".getBytes(StandardCharsets.UTF_8))).isEmpty();
    assertThat(ImageUtils.toPng(new byte[0])).isEmpty();
    assertThat(ImageUtils.toPng(null)).isEmpty();
  }

  @Test
  @DisplayName("SVG처럼 JDK ImageIO가 디코딩할 수 없는 포맷은 PNG로 변환할 수 없다")
  void toPng_emptyWhenUnsupportedFormat() throws IOException {
    byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"10\" height=\"10\"/>"
        .getBytes(StandardCharsets.UTF_8);
    assertThat(ImageUtils.toPng(svg)).isEmpty();
  }

  @Test
  @DisplayName("이미지의 가로, 세로 크기를 읽을 수 있다")
  void readImageSize_success() throws IOException {
    try (InputStream png = new ClassPathResource("sample-images/test.png").getInputStream()) {
      BufferedImage expected = ImageIO.read(
          new ClassPathResource("sample-images/test.png").getInputStream());
      assertThat(ImageUtils.readImageSize(png))
          .contains(new ImageSize(expected.getWidth(), expected.getHeight()));
    }
  }

  @Test
  @DisplayName("이미지가 아닌 데이터는 크기를 읽을 수 없다")
  void readImageSize_emptyWhenNotImage() throws IOException {
    InputStream text = new ByteArrayInputStream("not an image".getBytes(StandardCharsets.UTF_8));
    assertThat(ImageUtils.readImageSize(text)).isEmpty();
  }

  @Test
  @DisplayName("scaleDownToCover는 자르지 않고 비율을 유지한 채 영역을 덮는 최소 크기까지 축소한다")
  void scaleDownToCover_keepRatioAndCoverTarget() {
    BufferedImage result = ImageUtils.scaleDownToCover(
        new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB), 144, 288);
    assertThat(result.getWidth()).isEqualTo(384);
    assertThat(result.getHeight()).isEqualTo(288);
  }

  @Test
  @DisplayName("scaleDownToFit은 여백 없이 비율을 유지한 채 영역 안에 들어가는 최대 크기까지 축소한다")
  void scaleDownToFit_keepRatioWithinTarget() {
    BufferedImage result = ImageUtils.scaleDownToFit(
        new BufferedImage(600, 560, BufferedImage.TYPE_INT_ARGB), 124, 114);
    assertThat(result.getWidth()).isEqualTo(122);
    assertThat(result.getHeight()).isEqualTo(114);
  }

  @Test
  @DisplayName("축소 대상 영역보다 작은 이미지는 확대하지 않고 원본을 그대로 반환한다")
  void scaleDown_returnOriginalWhenSmallerThanTarget() {
    BufferedImage source = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    assertThat(ImageUtils.scaleDownToCover(source, 144, 288)).isSameAs(source);
    assertThat(ImageUtils.scaleDownToFit(source, 124, 114)).isSameAs(source);
  }

  @Test
  @DisplayName("여러 단계로 나눠 크게 축소해도 결과는 정확히 목표 크기다")
  void resizeFit_exactSizeWhenLargeDownscale() {
    BufferedImage result = ImageUtils.resizeFit(
        new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_ARGB), 48, 48);
    assertThat(result.getWidth()).isEqualTo(48);
    assertThat(result.getHeight()).isEqualTo(48);
  }

  private byte[] writeImage(BufferedImage image, String formatName) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, formatName, baos);
      return baos.toByteArray();
    }
  }
}
