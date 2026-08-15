package com.komentum.theme.android.utils;

import com.komentum.global.enums.FileExtension;
import com.komentum.theme.core.domain.ImageInset;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public final class NinePatchConverter {

  /**
   * 파일이 9-patch PNG라면 일반 PNG를 Android에서 사용하는 9-patch 형식으로 변환한다. 일반 PNG라면 별도 변환 없이 그대로 반환한다.
   */
  public static InputStream convertIfNeeded(
      InputStream inputStream,
      FileExtension extension,
      ImageInset imageInset
  ) throws IOException {
    // 9-patch-PNG가 아니라면 이미지 다시 반환
    if (extension != FileExtension.NINE_PATCH_PNG) {
      return inputStream;
    }
    // 9-patch image로 변환
    BufferedImage source = ImageIO.read(inputStream);
    if (source == null) {
      throw new IOException("[9-patch converter] Invalid source image");
    }
    BufferedImage result;
    if (imageInset == null) {
      result = createWithoutMarker(source);
    } else {
      result = convertWithMarker(source, imageInset);
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(result, "png", baos);
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /**
   * marker가 없는 9-patch PNG를 반환한다. inset이 없는 9-patch image의 경우 이 메서드를 사용한다.
   *
   */
  private static BufferedImage createWithoutMarker(BufferedImage source) {
    int width = source.getWidth();
    int height = source.getHeight();
    BufferedImage result = new BufferedImage(
        width + 2,
        height + 2,
        BufferedImage.TYPE_INT_ARGB
    );
    // 원본 이미지를 (1,1)위치에 복사하여 상하좌우 1px 여백을 남김
    Graphics2D graphics = result.createGraphics();
    graphics.drawImage(source, 1, 1, null);
    graphics.dispose();
    return result;
  }

  /**
   * 일반 PNG를 9-patch-PNG로 변환
   *
   */
  private static BufferedImage convertWithMarker(BufferedImage source, ImageInset imageInset) {
    // 상하좌우 1px씩 추가된 새로운 여백 이미지 생성
    int width = source.getWidth();
    int height = source.getHeight();
    BufferedImage result = createWithoutMarker(source);
    // inset이 유효하지 않다면 ( source의 width나 height의 범위를 벗어난다면 ) 예외 처리
    if (!imageInset.isInImageRange(width, height)) {
      throw new IllegalArgumentException(
          "[NinePatchConverter] Invalid image inset: inset out of bound");
    }
    /*
     * 위쪽 Border에 StretchX 영역에 대한 검은 선을 그린다
     * Android는 이 검은 선이 표시된 가로 구간만 이미지를 늘린다.
     */
    drawHorizontal(
        result,
        0,
        imageInset.getStretchX(),
        imageInset.getStretchX() + 1
    );
    /*
     * 왼쪽 Border에 Stretch 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 세로 구간만 이미지를 늘린다.
     */
    drawVertical(
        result,
        0,
        imageInset.getStretchY(),
        imageInset.getStretchY() + 1
    );
    /*
     * 아래쪽 Border에 Content 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 가로 구간 안에만 콘텐츠를 배치한다.
     */
    drawHorizontal(
        result,
        height + 1,
        imageInset.getEdgeInsetLeft(),
        width - imageInset.getEdgeInsetRight()
    );
    /*
     * 오른쪽 Border에 Content 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 세로 구간 안에만 콘텐츠를 배치한다.
     */
    drawVertical(
        result,
        width + 1,
        imageInset.getEdgeInsetTop(),
        height - imageInset.getEdgeInsetBottom()
    );
    return result;
  }

  /**
   * 9-patch 이미지의 Border에 가로 방향의 검은 선을 그린다.
   *
   * @param y     검은 선을 그릴 Border의 y 좌표 (상단 또는 하단 Border)
   * @param start 검은 선 시작 x 좌표
   * @param end   검은 선 종료 x 좌표
   */
  private static void drawHorizontal(
      BufferedImage image,
      int y,
      int start,
      int end
  ) {
    for (int x = start; x <= end; x++) {
      image.setRGB(x, y, Color.BLACK.getRGB());
    }
  }

  /**
   * 9-patch 이미지의 Border에 세로 방향의 검은 선을 그린다.
   *
   * @param x     검은 선을 그릴 Border의 x 좌표 (좌측 또는 우측 Border)
   * @param start 검은 선 시작 y 좌표
   * @param end   검은 선 종료 y 좌표
   */
  private static void drawVertical(
      BufferedImage image,
      int x,
      int start,
      int end
  ) {
    for (int y = start; y <= end; y++) {
      image.setRGB(x, y, Color.BLACK.getRGB());
    }
  }
}
