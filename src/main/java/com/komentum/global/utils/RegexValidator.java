package com.komentum.global.utils;

import java.util.regex.Pattern;

public class RegexValidator {

  private final static Pattern HEX_COLOR_PATTERN = Pattern.compile("^#?[0-9a-fA-F]{8}$");

  public static boolean isValidHexColor(String hexColor) {
    return hexColor != null && HEX_COLOR_PATTERN.matcher(hexColor).matches();
  }
}
