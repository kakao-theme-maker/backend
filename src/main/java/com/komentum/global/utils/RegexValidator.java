package com.komentum.global.utils;

import java.util.regex.Pattern;

public class RegexValidator {

  public static final String HEX_COLOR_REGEX = "^#?[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$";

  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(HEX_COLOR_REGEX);

  public static boolean isValidHexColor(String hexColor) {
    return hexColor != null && HEX_COLOR_PATTERN.matcher(hexColor).matches();
  }

  /**
   * origin이 허용된 origin 패턴 목록에 포함되는지 확인한다.
   * 허용 패턴은 CORS allowed-origin-list와 동일한 형식(예: "https://*.vercel.app")을 사용하며,'*'를 와일드카드로 갖는 패턴을 regex로 변환하여 비교한다.
   * origin은 URL이 아닌 도메인 주소이어야 한다.
   * @param origin 검사할 origin (scheme://host[:port])
   * @param allowedOrigins 허용된 origin 패턴 목록
   */
  public static boolean isAllowedOrigin(String origin, String[] allowedOrigins) {
    if (origin == null || allowedOrigins == null) {
      return false;
    }
    for (String allowedOrigin : allowedOrigins) {
      String originRegex = "^" + Pattern.quote(allowedOrigin).replace("*", "\\E.*\\Q") + "$";
      if (Pattern.matches(originRegex, origin)) {
        return true;
      }
    }
    return false;
  }
}
