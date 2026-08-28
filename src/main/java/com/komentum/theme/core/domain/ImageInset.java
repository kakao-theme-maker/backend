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
