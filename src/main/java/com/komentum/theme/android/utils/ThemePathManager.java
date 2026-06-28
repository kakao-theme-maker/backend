package com.komentum.theme.android.utils;

import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.android.service.AndroidThemeGenerator;
import java.nio.file.Path;
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

  /**
   * get a specific theme's directory
   */
  public static Path getThemeDir(String themeId) {
    return Path.of(getBasePath())
        .resolve("theme")
        .resolve("android")
        .resolve(themeId);
  }

  /**
   * get a specific theme's sample source apk directory
   */
  public static Path getThemeSourceDir(String themeId) {
    return getThemeDir(themeId)
        .resolve("source");
  }

  public static Path getAndroidThemeImagePath(String themeId, AndroidComponentDto component) {
    return getThemeSourceDir(themeId)
        .resolve("src")
        .resolve("main")
        .resolve("theme")
        .resolve(component.getImageFilePath());
  }

  public static Path getAndroidColorSheetPath(String themeId) {
    return getThemeSourceDir(themeId)
        .resolve("src")
        .resolve("main")
        .resolve("theme")
        .resolve("values")
        .resolve("colors.xml");
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
