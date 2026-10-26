package com.komentum.theme.android.utils;

import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.android.properties.AndroidThemeProperties;
import com.komentum.theme.android.service.AndroidThemeGenerator;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemePathManager {

  private final AndroidThemeProperties androidThemeProperties;

  /**
   * get a root directory path of the theme
   */
  private Path getBasePath() {
    Path path = Paths.get("").toAbsolutePath();
    return path.resolve(androidThemeProperties.getBasePath());
  }

  public Path getDefaultThemeDir(String buildIdentifier) {
    return getBasePath()
        .resolve("theme")
        .resolve("default")
        .resolve(buildIdentifier);
  }

  /**
   * get a specific theme's directory
   */
  public Path getThemeDir(String buildIdentifier) {
    return getBasePath()
        .resolve("theme")
        .resolve("android")
        .resolve(buildIdentifier);
  }

  /**
   * get a specific theme's sample source apk directory
   */
  public Path getThemeSourceDir(String buildIdentifier) {
    return getThemeDir(buildIdentifier)
        .resolve("source");
  }

  public Path getAndroidThemeImagePath(String buildIdentifier, AndroidComponentDto component) {
    Path themeSourcePath = getThemeSourceDir(buildIdentifier);
    return getAndroidThemeImagePath(themeSourcePath, component.getImageFilePath());
  }

  public Path getAndroidThemeImagePath(Path themeSourcePath, String imageFilePath) {
    return themeSourcePath
        .resolve("src")
        .resolve("main")
        .resolve(imageFilePath);
  }

  public Path getAndroidColorSheetPath(String buildIdentifier) {
    Path themeSourcePath = getThemeSourceDir(buildIdentifier);
    return getAndroidColorSheetPath(themeSourcePath);
  }

  public Path getAndroidColorSheetPath(Path themeSourcePath) {
    return themeSourcePath
        .resolve("src")
        .resolve("main")
        .resolve("theme")
        .resolve("values")
        .resolve("colors.xml");
  }

  public Path getAndroidResourcePath(String buildIdentifier) {
    return getThemeSourceDir(buildIdentifier)
        .resolve("src")
        .resolve("main")
        .resolve("theme");
  }

  /**
   * <p>테마 폴더의 빌드 결과물에 있는 빌드 결과물 경로를 반환한다.</p>
   * <p>빌드 결과물의 이름은 docker 내 테마 폴더 이름을 따라간다.</p>
   * */
  public Path getAndroidThemeOutputPath(String buildIdentifier) {
    return getThemeSourceDir(buildIdentifier)
        .resolve("build")
        .resolve("outputs")
        .resolve("apk")
        .resolve("release")
        .resolve(AndroidThemeGenerator.DOCKER_THEME_DIRECTORY_NAME + "-release.apk");
  }
}
