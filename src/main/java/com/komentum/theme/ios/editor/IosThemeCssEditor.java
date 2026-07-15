package com.komentum.theme.ios.editor;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.ios.utils.IosThemePathManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class IosThemeCssEditor {

  private static final String MANIFEST_STYLE = "ManifestStyle";
  private static final String THEME_NAME = "-kakaotalk-theme-name";
  private static final String THEME_VERSION = "-kakaotalk-theme-version";
  private static final String AUTHOR_NAME = "-kakaotalk-author-name";
  private static final String THEME_ID = "-kakaotalk-theme-id";

  public void editCss(
      Path workDir,
      ThemeComponent themeComponent,
      List<ThemeStyle> themeStyles,
      List<PlatformColorStyle> platformColorStyles
  ) throws IOException {
    if (platformColorStyles == null || platformColorStyles.isEmpty()) {
      throw new IllegalStateException("iOS platform color style seed is required");
    }
    Path cssPath = IosThemePathManager.getCssPath(workDir);
    String css = Files.readString(cssPath, StandardCharsets.UTF_8);
    css = editManifest(css, themeComponent);
    css = editStyles(css, themeStyles, platformColorStyles);
    Files.writeString(cssPath, css, StandardCharsets.UTF_8);
  }

  private String editManifest(String css, ThemeComponent themeComponent) {
    String result = css;
    result = replaceCssProperty(result, MANIFEST_STYLE, THEME_NAME,
        quote(themeComponent.getThemeName()));
    result = replaceCssProperty(result, MANIFEST_STYLE, THEME_VERSION,
        quote(resolveThemeVersion(themeComponent)));
    result = replaceCssProperty(result, MANIFEST_STYLE, AUTHOR_NAME,
        quote(themeComponent.getUserEmail()));
    result = replaceCssProperty(result, MANIFEST_STYLE, THEME_ID,
        quote("com.komentum.theme.ios.t" + themeComponent.getThemeComponentId()));
    return result;
  }

  private String editStyles(
      String css,
      List<ThemeStyle> themeStyles,
      List<PlatformColorStyle> platformColorStyles
  ) {
    Map<Integer, ThemeStyle> themeStyleMap = themeStyles.stream()
        .collect(Collectors.toMap(
            themeStyle -> themeStyle.getColorStyle().getColorStyleId(),
            themeStyle -> themeStyle,
            (left, right) -> left
        ));
    String result = css;
    // iOS 시드의 resourceGroup과 resourceName을 각각 CSS 선택자와 속성으로 사용한다.
    // 대응하는 테마 색상이 없거나 유효하지 않으면 템플릿 기본 색상을 유지한다.
    for (PlatformColorStyle platformColorStyle : platformColorStyles) {
      ThemeStyle themeStyle = themeStyleMap.get(
          platformColorStyle.getColorStyle().getColorStyleId());
      if (themeStyle == null) {
        continue;
      }
      String color = normalizeCssColor(themeStyle.getColor());
      if (color == null) {
        continue;
      }
      result = replaceCssProperty(
          result,
          platformColorStyle.getResourceGroup(),
          platformColorStyle.getResourceName(),
          color
      );
    }
    return result;
  }

  private String replaceCssProperty(String css, String selector, String property, String value) {
    // 전달받은 선택자 블록 안에서 지정한 속성의 값만 정규식으로 교체한다.
    // 치환 그룹으로 속성 앞부분과 세미콜론을 보존하며,
    // 선택자와 여는 중괄호 사이의 블록 주석도 허용한다.
    Pattern pattern = Pattern.compile(
        "(" + Pattern.quote(selector) + "(?:\\s|/\\*.*?\\*/)*\\{[^}]*?"
            + Pattern.quote(property)
            + "\\s*:\\s*)[^;]*(;)",
        Pattern.DOTALL
    );
    Matcher matcher = pattern.matcher(css);
    if (!matcher.find()) {
      return css;
    }
    return matcher.replaceAll("$1" + Matcher.quoteReplacement(value) + "$2");
  }

  private String quote(String value) {
    // 매니페스트 값은 작은따옴표로 감싸므로 입력값의 작은따옴표를 제거해
    // CSS 문자열 선언이 중간에서 끊어지지 않도록 한다.
    return "'" + (value == null ? "" : value.replace("'", "")) + "'";
  }

  private String resolveThemeVersion(ThemeComponent themeComponent) {
    if (themeComponent.getVersionName() != null && !themeComponent.getVersionName().isBlank()) {
      return themeComponent.getVersionName();
    }
    return themeComponent.getVersionNumber();
  }

  private String normalizeCssColor(String color) {
    if (color == null || color.isBlank()) {
      return null;
    }
    String normalized = color.trim();
    if (normalized.matches("[0-9a-fA-F]{6}")) {
      return "#" + normalized;
    }
    // 공통 테마의 8자리 색상은 RRGGBBAA 형식이다.
    // 현재 iOS CSS에는 알파를 반영하지 않으므로 뒤의 AA를 제외한 #RRGGBB만 기록한다.
    if (normalized.matches("[0-9a-fA-F]{8}")) {
      return "#" + normalized.substring(0, 6);
    }
    if (normalized.matches("#[0-9a-fA-F]{8}")) {
      return normalized.substring(0, 7);
    }
    if (normalized.matches("#[0-9a-fA-F]{6}")) {
      return normalized;
    }
    return null;
  }
}
