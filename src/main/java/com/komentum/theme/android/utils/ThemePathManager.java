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

  public static Path getAndroidResourcePath(String themeId) {
    return getThemeSourceDir(themeId)
        .resolve("src")
        .resolve("main")
        .resolve("theme");
  }

  /**
   * <p>테마 폴더의 빌드 결과물에 있는 빌드 결과물 경로를 반환한다.</p>
   * <p>빌드 결과물의 이름은 docker 내 테마 폴더 이름을 따라간다.</p>
   * */
  public static Path getAndroidThemeOutputPath(String themeId) {
    return getThemeSourceDir(themeId)
        .resolve("build")
        .resolve("outputs")
        .resolve("apk")
        .resolve("release")
        .resolve(AndroidThemeGenerator.DOCKER_THEME_DIRECTORY_NAME + "-release.apk");
  }
}
