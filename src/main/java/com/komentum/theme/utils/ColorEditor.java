package com.komentum.theme.utils;

import com.komentum.global.utils.RegexValidator;

public class ColorEditor {

  /**
   * 주어진 색상의 RGB에 weight를 곱하여 어두운 6자리 hex color를 반환한다
   * @param hexColor 색상 string ( #000000 or 000000 )
   * @param weight 가중치 ( 낮을수록 어두워짐 )
   * */
  public static String toDarkColor(String hexColor, double weight) {
    if (!RegexValidator.isValidHexColor(hexColor)) {
      throw new IllegalArgumentException("Invalid hex color: " + hexColor);
    }
    // parse color
    String colorString = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
    int colorInt1 = (int) (Integer.parseInt(colorString.substring(0, 2), 16) * weight);
    int colorInt2 = (int) (Integer.parseInt(colorString.substring(2, 4), 16) * weight);
    int colorInt3 = (int) (Integer.parseInt(colorString.substring(4, 6), 16) * weight);
    return String.format("#%02X%02X%02X",
        colorInt1,
        colorInt2,
        colorInt3);
  }
}
