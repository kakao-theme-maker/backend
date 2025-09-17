package com.komentum.global.utils;

public class NumberUtils {

  public static boolean isNumericString(String str) {
    return str != null && str.matches("-?\\d+");
  }
}
