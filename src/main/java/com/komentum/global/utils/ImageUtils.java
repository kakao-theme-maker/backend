package com.komentum.global.utils;

import com.komentum.global.enums.FileExtension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtils {

  public static boolean compareImages(BufferedImage image1, BufferedImage image2) {
    int width = image1.getWidth();
    int height = image1.getHeight();
    if (image2.getWidth() != width || image2.getHeight() != height) {
      return false;
    }
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 파일 헤더(매직 바이트)를 읽어 실제 이미지 포맷을 판별하고, 대응하는 FileExtension을 반환한다. Content-Type 헤더나 파일 확장자는
   * 신뢰하지 않는다. 테마 빌드에서 사용하는 FileExtension과 포맷명이 일치하는 이미지(PNG)만 지원한다.
   *
   * @param is 판별할 이미지의 InputStream, 호출자가 닫아야 한다
   * @return 지원하는 이미지 포맷이면 FileExtension, 아니면 Optional.empty()
   * @throws IOException 스트림을 읽지 못한 경우
   */
  public static Optional<FileExtension> detectImageExtension(InputStream is) throws IOException {
    if (is == null) {
      return Optional.empty();
    }
    try (ImageInputStream iis = new MemoryCacheImageInputStream(is)) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
      while (readers.hasNext()) {
        ImageReader reader = readers.next();
        String formatName = reader.getFormatName();
        reader.dispose();
        Optional<FileExtension> extension = Arrays.stream(FileExtension.values())
            .filter(fileExtension -> fileExtension.getExtension().equalsIgnoreCase(formatName))
            .findFirst();
        if (extension.isPresent()) {
          return extension;
        }
      }
      return Optional.empty();
    }
  }

  /**
   * 이미지를 PNG 바이트로 변환한다. 실제 포맷(매직 바이트)이 이미 PNG이면 원본을 그대로 반환하고, JDK ImageIO가 디코딩할 수 있는
   * 다른 포맷(JPEG, GIF, BMP, WBMP, TIFF)이면 PNG로 다시 인코딩한다. 애니메이션 GIF는 첫 프레임만 변환된다.
   *
   * @param imageBytes 변환할 이미지 바이트
   * @return PNG 바이트, 이미지가 아니거나 디코딩할 수 없는 포맷(CMYK JPEG, SVG, WebP 등)이면 Optional.empty()
   * @throws IOException PNG로 인코딩하지 못한 경우
   */
  public static Optional<byte[]> toPng(byte[] imageBytes) throws IOException {
    if (imageBytes == null || imageBytes.length == 0) {
      return Optional.empty();
    }
    if (detectImageExtension(new ByteArrayInputStream(imageBytes))
        .filter(FileExtension.PNG::equals).isPresent()) {
      return Optional.of(imageBytes);
    }
    BufferedImage image;
    try {
      image = ImageIO.read(new ByteArrayInputStream(imageBytes));
    } catch (IIOException e) {
      log.warn("[ImageUtils] failed to decode image : {}", e.getMessage());
      return Optional.empty();
    }
    if (image == null) {
      return Optional.empty();
    }
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      if (!ImageIO.write(image, FileExtension.PNG.getExtension(), baos)) {
        return Optional.empty();
      }
      return Optional.of(baos.toByteArray());
    }
  }

  /**
   * 이미지의 가로, 세로 크기를 파일 헤더에서 읽는다. 이미지 전체를 디코딩하지 않는다.
   *
   * @param is 크기를 읽을 이미지의 InputStream, 호출자가 닫아야 한다
   * @return 이미지 크기, 이미지 포맷이 아니라면 Optional.empty()
   * @throws IOException 스트림을 읽지 못한 경우
   */
  public static Optional<ImageSize> readImageSize(InputStream is) throws IOException {
    if (is == null) {
      return Optional.empty();
    }
    try (ImageInputStream iis = new MemoryCacheImageInputStream(is)) {
      Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
      if (!readers.hasNext()) {
        return Optional.empty();
      }
      ImageReader reader = readers.next();
      try {
        reader.setInput(iis, true, true);
        return Optional.of(new ImageSize(reader.getWidth(0), reader.getHeight(0)));
      } finally {
        reader.dispose();
      }
    }
  }

  public record ImageSize(int width, int height) {

  }
}
