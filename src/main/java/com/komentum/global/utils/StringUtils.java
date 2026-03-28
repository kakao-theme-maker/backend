package com.komentum.global.utils;

public class StringUtils {

  public static String removeTrailingSlash(String target) {
    if (target == null || target.isEmpty()) {
      return target;
    }
    return target.endsWith("/") ? target.substring(0, target.length() - 1) : target;
  }

  public static String trimSlash(String target) {
    if (target == null || target.isEmpty()) {
      return target;
    }
    return target.replaceAll("^/+|/$+", "");
  }
}
