package com.komentum.theme.android.utils;

import com.komentum.theme.android.dto.AndroidComponentDto;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class ThemePathManager {

  /**
   * get a root directory path of the theme
   */
  private static String getBasePath() {
    String os = System.getProperty("os.name");
    if (os.toLowerCase().startsWith("win")) {
      return "C:\\tmp";
    } else {
      return "/tmp";
    }
  }

  public static Path getDefaultThemeDir(Integer themeComponentId) {
    return Path.of(getBasePath())
        .resolve("theme")
        .resolve("default")
        .resolve(themeComponentId.toString());
  }

  /**
   * get a specific theme's directory
   */
  public static Path getThemeDir(String themeId) {
    Path basePath = Path.of(getBasePath());
    return Paths.get(basePath.toAbsolutePath().toString(), "theme", "android", themeId);
  }

  /**
   * get a specific theme's sample source apk directory
   */
  public static Path getThemeSourceDir(String themeId) {
    return Paths.get(getThemeDir(themeId).toString(), "source");
  }

  /**
   * get a specific theme's depacked theme directory
   */
  public static Path getThemeDepackedDir(String themeId) {
    return Paths.get(getThemeDir(themeId).toString(), "depack");
  }

  /**
   * get a specific theme's repackaged theme directory
   */
  public static Path getThemeRepackedDir(String themeId) {
    return Paths.get(getThemeDir(themeId).toString(), "repack");
  }

  /**
   * get a specific theme's depacked image path
   */
  public static Path getImagePath(String themeId, AndroidComponentDto component) {
    return Paths.get(getThemeDepackedDir(themeId).toString(), component.getAndroidComponentPath());
  }

  /**
   * get a specific theme's depacked color sheet path
   */
  public static Path getColorSheetPath(String themeId) {
    return Paths.get(getThemeDepackedDir(themeId).toString(), "res", "values", "colors.xml");
  }

  public static Path getThemeResourcePath(String themeId) {
    return Paths.get(getThemeDepackedDir(themeId).toString(), "res");
  }
}
