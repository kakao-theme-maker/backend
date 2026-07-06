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
    Pattern pattern = Pattern.compile(
        "(" + Pattern.quote(selector) + "\\s*\\{[^}]*?" + Pattern.quote(property)
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
    if (normalized.matches("#[0-9a-fA-F]{8}")) {
      return normalized.substring(0, 7);
    }
    if (normalized.matches("#[0-9a-fA-F]{6}")) {
      return normalized;
    }
    return null;
  }
}
