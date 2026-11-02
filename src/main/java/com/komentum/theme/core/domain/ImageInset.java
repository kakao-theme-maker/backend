package com.komentum.theme.core.domain;

import com.komentum.theme.core.dto.ThemeUpdateRequest.InsetUpdateDto;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImageInset {

  // 말풍선 내 콘텐츠가 시작되는 stretch 좌표 ( 이미지가 늘어나기 시작하는 기준 좌표 )
  private Integer stretchX;
  private Integer stretchY;

  // 콘텐츠 영역의 edge inset(padding)
  private Integer edgeInsetTop;
  private Integer edgeInsetLeft;
  private Integer edgeInsetBottom;
  private Integer edgeInsetRight;

  public boolean hasAllValue() {
    return stretchX != null &&
        stretchY != null &&
        edgeInsetTop != null &&
        edgeInsetLeft != null &&
        edgeInsetBottom != null &&
        edgeInsetRight != null;
  }

  public boolean isInImageRange(int width, int height) {
    return hasAllValue() &&
        (stretchX >= 0 && stretchX <= width) &&
        (stretchY >= 0 && stretchY <= height) &&
        (edgeInsetTop >= 0 && edgeInsetTop <= height) &&
        (edgeInsetLeft >= 0 && edgeInsetLeft <= width) &&
        (edgeInsetBottom >= 0 && edgeInsetBottom <= height) &&
        (edgeInsetRight >= 0 && edgeInsetRight <= width);
  }

  /**
   * 원본 이미지 크기 기준의 inset을 리사이즈된 이미지 크기 기준으로 변환한 새 inset을 반환한다. 가로 값은 가로 배율, 세로 값은 세로 배율로
   * 변환하며 이미지 범위 안으로 보정한다. 원본에 콘텐츠 영역이 있었다면 반올림으로 사라지지 않도록 최소 1px을 남긴다. 이 객체는 DB에 저장된
   * 값이므로 수정하지 않는다.
   *
   * @throws IllegalStateException    inset 값이 하나라도 없는 경우
   * @throws IllegalArgumentException 이미지 크기가 0 이하인 경우
   */
  public ImageInset scale(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
    if (!hasAllValue()) {
      throw new IllegalStateException(
          "[ImageInset] failed to scale inset : inset value is missing");
    }
    if (sourceWidth <= 0 || sourceHeight <= 0 || targetWidth <= 0 || targetHeight <= 0) {
      throw new IllegalArgumentException("[ImageInset] failed to scale inset : invalid image size");
    }
    int[] horizontal = scaleEdgeInsets(edgeInsetLeft, edgeInsetRight, sourceWidth,
        targetWidth); // [scaledLeft, scaledRight]
    int[] vertical = scaleEdgeInsets(edgeInsetTop, edgeInsetBottom, sourceHeight,
        targetHeight); // [scaledTop, scaledBottom]
    return ImageInset.builder()
        .stretchX(scaleValue(stretchX, sourceWidth, targetWidth))
        .stretchY(scaleValue(stretchY, sourceHeight, targetHeight))
        .edgeInsetLeft(horizontal[0])
        .edgeInsetRight(horizontal[1])
        .edgeInsetTop(vertical[0])
        .edgeInsetBottom(vertical[1])
        .build();
  }

  /**
   * 한 축의 양쪽 edge inset을 변환한다. 원본에 콘텐츠 영역이 있었는데 반올림으로 사라지면 끝쪽 inset을 줄여 최소 1px을 남긴다.
   *
   * @return [시작쪽 inset, 끝쪽 inset]
   */
  private static int[] scaleEdgeInsets(int start, int end, int sourceLength, int targetLength) {
    int scaledStart = scaleValue(start, sourceLength, targetLength);
    int scaledEnd = scaleValue(end, sourceLength, targetLength);
    // 기존 inset은 정상 범위인데, 스케일링된 inset이 정상 범위가 아니라면 최소한의 값을 남긴다
    if (sourceLength - start - end >= 1 && targetLength - scaledStart - scaledEnd < 1) {
      scaledEnd = Math.max(0, targetLength - scaledStart - 1);
    }
    return new int[]{scaledStart, scaledEnd};
  }

  private static int scaleValue(int value, int sourceLength, int targetLength) {
    int scaled = (int) Math.round((double) value * targetLength / sourceLength);
    return Math.min(Math.max(scaled, 0), targetLength);
  }

  public static ImageInset from(InsetUpdateDto insetUpdateDto) {
    return ImageInset.builder()
        .stretchX(insetUpdateDto.getStretchX())
        .stretchY(insetUpdateDto.getStretchY())
        .edgeInsetTop(insetUpdateDto.getTop())
        .edgeInsetLeft(insetUpdateDto.getLeft())
        .edgeInsetBottom(insetUpdateDto.getBottom())
        .edgeInsetRight(insetUpdateDto.getRight())
        .build();
  }
}
