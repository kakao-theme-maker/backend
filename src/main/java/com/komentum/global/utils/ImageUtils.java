package com.komentum.global.utils;

import com.komentum.global.enums.FileExtension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

  /**
   * 원본 비율을 유지한 채 대상 영역 전체를 덮도록 크기를 맞춘 뒤, 넘치는 부분을 중앙 기준으로 잘라 width x height 이미지를 만든다. 투명 여백이
   * 생기지 않는다.
   */
  public static BufferedImage resizeCenterCrop(BufferedImage source, int width, int height) {
    double scale = Math.max((double) width / source.getWidth(),
        (double) height / source.getHeight());
    int scaledWidth = (int) Math.ceil(source.getWidth() * scale);
    int scaledHeight = (int) Math.ceil(source.getHeight() * scale);
    return drawOnCanvas(drawScaled(source, scaledWidth, scaledHeight), width, height);
  }

  /**
   * 원본 비율을 유지한 채 이미지 전체가 대상 영역 안에 들어가도록 크기를 맞추고, 남는 영역은 투명 여백으로 채워 width x height 이미지를
   * 만든다.
   */
  public static BufferedImage resizeFit(BufferedImage source, int width, int height) {
    double scale = Math.min((double) width / source.getWidth(),
        (double) height / source.getHeight());
    int scaledWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
    int scaledHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
    return drawOnCanvas(drawScaled(source, scaledWidth, scaledHeight), width, height);
  }

  /**
   * 원본 비율을 유지한 채 width x height 영역을 덮는 최소 크기까지 축소한다. 자르거나 여백을 넣지 않으므로 결과는 항상 영역 이상의 크기이며,
   * 이미 영역 이하의 크기라면 원본을 그대로 반환한다
   */
  public static BufferedImage scaleDownToCover(BufferedImage source, int width, int height) {
    double scale = Math.max((double) width / source.getWidth(),
        (double) height / source.getHeight());
    return scaleDown(source, scale);
  }

  /**
   * 원본 비율을 유지한 채 width x height 영역 안에 들어가는 최대 크기까지 축소한다. 자르거나 여백을 넣지 않으며, 이미 영역 안에 들어가는
   * 크기라면 원본을 그대로 반환한다.
   */
  public static BufferedImage scaleDownToFit(BufferedImage source, int width, int height) {
    double scale = Math.min((double) width / source.getWidth(),
        (double) height / source.getHeight());
    return scaleDown(source, scale);
  }

  /**
   * scale이 1보다 작을 때만 원본 비율을 유지한 채 축소한다.
   * 이는 이미지 크기를 줄이는 메서드에서 결과 이미지의 크기를 늘리지 않기 위함이다.
   */
  private static BufferedImage scaleDown(BufferedImage source, double scale) {
    if (scale >= 1) {
      return source;
    }
    int scaledWidth = Math.max(1, (int) Math.round(source.getWidth() * scale));
    int scaledHeight = Math.max(1, (int) Math.round(source.getHeight() * scale));
    return drawScaled(source, scaledWidth, scaledHeight);
  }

  /**
   * 이미지를 width x height 크기로 다시 그린다. 한 번에 크게 축소하면 bicubic 보간에서 계단 현상이 생기므로, 목표 크기의 2배 이내가 될
   * 때까지 절반씩 나눠 축소한다.
   */
  private static BufferedImage drawScaled(BufferedImage source, int width, int height) {
    BufferedImage current = source;
    int currentWidth = source.getWidth();
    int currentHeight = source.getHeight();
    // 이미지를 절반씩 크기를 줄인다.
    while (currentWidth / 2 >= width && currentHeight / 2 >= height) {
      currentWidth /= 2;
      currentHeight /= 2;
      current = draw(current, currentWidth, currentHeight);
    }
    // 최종적으로 width * height 크기의 이미지를 반환한다
    return draw(current, width, height);
  }

  /**
   * 이미지를 width x height 투명 캔버스의 중앙에 원본 크기 그대로 그린다. 캔버스보다 큰 부분은 잘리고, 작은 부분은 투명 여백이 된다.
   */
  private static BufferedImage drawOnCanvas(BufferedImage image, int width, int height) {
    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = output.createGraphics();
    graphics.drawImage(image, (width - image.getWidth()) / 2, (height - image.getHeight()) / 2,
        null);
    graphics.dispose();
    return output;
  }

  /**
   * source 이미지를 width * height 크기로 스케일링하여 반환한다
   * */
  private static BufferedImage draw(BufferedImage source, int width, int height) {
    BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = output.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics.drawImage(source, 0, 0, width, height, null);
    graphics.dispose();
    return output;
  }

  public record ImageSize(int width, int height) {

  }
}
