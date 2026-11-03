package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.enums.FileExtension;
import com.komentum.global.utils.ImageUtils.ImageSize;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
}
