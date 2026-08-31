package com.komentum.theme.core.domain;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.global.utils.RegexValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "theme_style", uniqueConstraints = {
    @UniqueConstraint(name = "THEME_COMPONENT_COLOR_STYLE_UNIQUE", columnNames = {
        "theme_component_id", "color_style_id"})})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeStyle {

  private static final int MIN_ALPHA = 0;
  private static final int MAX_ALPHA = 100;
  private static final int MAX_HEX_ALPHA = 255;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long themeStyleId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "theme_component_id")
  private ThemeComponent themeComponent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "color_style_id")
  private ColorStyle colorStyle;

  @Setter(AccessLevel.NONE)
  @Column(name = "color")
  private String color;

  /**
   * 색상의 백분율 투명도(0~100). null을 허용하지 않으며 항상 color와 함께 관리된다.
   */
  @Setter(AccessLevel.NONE)
  @Builder.Default
  @Column(name = "alpha", nullable = false)
  private Integer alpha = MAX_ALPHA;

  public static ThemeStyle copyOf(ThemeComponent targetTheme, ThemeStyle sourceStyle) {
    return ThemeStyle.builder()
        .themeComponent(targetTheme)
        .colorStyle(sourceStyle.getColorStyle())
        .color(sourceStyle.getColor())
        .alpha(sourceStyle.getAlpha())
        .build();
  }

  /**
   * 요청으로 전달된 color와 alpha 값으로 기존 스타일 값을 덮어쓴다.
   * color와 alpha는 항상 함께 존재해야 하며, 어느 하나만 존재하는 상태는 허용하지 않는다.
   *
   * @param color 새로 적용할 색상 값 (null 불가)
   * @param alpha 새로 적용할 백분율 투명도, 0~100 (null 불가)
   * @throws IllegalArgumentException color 또는 alpha가 null이거나 alpha가 0~100 범위를 벗어난 경우
   */
  public void updateColorAndAlpha(String color, Integer alpha) {
    if (color == null || alpha == null) {
      throw new IllegalArgumentException("color and alpha must not be null and must be provided together");
    }
    if (alpha < MIN_ALPHA || alpha > MAX_ALPHA) {
      throw new IllegalArgumentException("alpha must be between " + MIN_ALPHA + " and " + MAX_ALPHA);
    }
    this.color = color;
    this.alpha = alpha;
  }

  /**
   * 백분율 투명도(alpha)를 16진수 값으로 변환해 color에 적용한 iOS용 RGBA 색상 값을 반환한다.
   * color가 null/공백이거나 유효한 hex 형식이 아니면 null을 반환한다.
   *
   * @return "#RRGGBBAA" 형식의 색상 문자열, 변환할 수 없으면 null
   */
  public String getColorWithAlphaRgba() {
    String baseColor = extractBaseHexColor();
    if (baseColor == null) {
      return null;
    }
    return "#" + baseColor + toHexAlpha();
  }

  /**
   * 백분율 투명도(alpha)를 16진수 값으로 변환해 color에 적용한 Android용 ARGB 색상 값을 반환한다.
   * color가 null/공백이거나 유효한 hex 형식이 아니면 null을 반환한다.
   *
   * @return "#AARRGGBB" 형식의 색상 문자열, 변환할 수 없으면 null
   */
  public String getColorWithAlphaArgb() {
    String baseColor = extractBaseHexColor();
    if (baseColor == null) {
      return null;
    }
    return "#" + toHexAlpha() + baseColor;
  }

  private String extractBaseHexColor() {
    if (color == null || alpha == null || color.isBlank()
        || !RegexValidator.isValidHexColor(color.trim())) {
      return null;
    }
    String hex = color.trim();
    hex = hex.startsWith("#") ? hex.substring(1) : hex;
    return hex.substring(0, 6).toUpperCase();
  }

  private String toHexAlpha() {
    int hexAlpha = Math.round(alpha * MAX_HEX_ALPHA / (float) MAX_ALPHA);
    return String.format("%02X", hexAlpha);
  }
}