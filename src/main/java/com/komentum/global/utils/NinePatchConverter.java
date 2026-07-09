package com.komentum.global.utils;

import com.komentum.global.enums.FileExtension;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public final class NinePatchConverter {

  // stretch 영역을 이미지의 40 ~ 60% 영역으로 지정
  // 9-patch PNG의 검은 선으로 표시되는 영역 ( 확장되는 영역 )
  private static final double STRETCH_START_RATIO = 0.4;
  private static final double STRETCH_END_RATIO = 0.6;

  // content 영역을 이미지의 20 ~ 80% 영역으로 지정
  // 실제 콘텐츠 및 텍스트가 배치될 수 있는 영역
  private static final double CONTENT_START_RATIO = 0.2;
  private static final double CONTENT_END_RATIO = 0.8;

  /**
   * 파일이 9-patch PNG라면 일반 PNG를 Android에서 사용하는
   * 9-patch 형식으로 변환한다.
   * 일반 PNG라면 별도 변환 없이 그대로 반환한다.
   */
  public static InputStream convertIfNeeded(
      InputStream inputStream,
      FileExtension extension
  ) throws IOException {
    // 9-patch-PNG가 아니라면 이미지 다시 반환
    if (extension != FileExtension.NINE_PATCH_PNG) {
      return inputStream;
    }
    // InputStream 이미지를 9-patch-PNG로 변환
    BufferedImage source = ImageIO.read(inputStream);
    if (source == null) {
      throw new IOException("Invalid image");
    }
    BufferedImage result = convert(source);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(result, "png", baos);
    return new ByteArrayInputStream(baos.toByteArray());
  }

  /**
   * 일반 PNG를 9-patch-PNG로 변환
   * */
  private static BufferedImage convert(BufferedImage source) {
    // 상하좌우 1px씩 추가된 새로운 이미지 생성
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
    /*
     * 위쪽 Border에 Stretch 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 가로 구간만 이미지를 늘린다.
     */
    drawHorizontal(
        result,
        1,
        ratio(width, STRETCH_START_RATIO),
        ratio(width, STRETCH_END_RATIO)
    );
    /*
     * 왼쪽 Border에 Stretch 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 세로 구간만 이미지를 늘린다.
     */
    drawVertical(
        result,
        1,
        ratio(height, STRETCH_START_RATIO),
        ratio(height, STRETCH_END_RATIO)
    );
    /*
     * 아래쪽 Border에 Content 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 가로 구간 안에만 콘텐츠를 배치한다.
     */
    drawHorizontal(
        result,
        height + 1,
        ratio(width, CONTENT_START_RATIO),
        ratio(width, CONTENT_END_RATIO)
    );
    /*
     * 오른쪽 Border에 Content 영역을 나타내는 검은 선을 그린다.
     * Android는 이 검은 선이 표시된 세로 구간 안에만 콘텐츠를 배치한다.
     */
    drawVertical(
        result,
        width + 1,
        ratio(height, CONTENT_START_RATIO),
        ratio(height, CONTENT_END_RATIO)
    );
    return result;
  }

  private static int ratio(int size, double ratio) {
    return Math.max(0, Math.min(size - 1, (int) Math.round(size * ratio)));
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
      image.setRGB(x + 1, y, Color.BLACK.getRGB());
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
      image.setRGB(x, y + 1, Color.BLACK.getRGB());
    }
  }
}
