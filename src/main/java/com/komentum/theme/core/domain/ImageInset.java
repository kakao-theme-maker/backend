package com.komentum.theme.core.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
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
}
