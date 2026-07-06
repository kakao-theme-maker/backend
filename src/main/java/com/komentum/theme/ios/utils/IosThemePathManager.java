package com.komentum.theme.ios.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class IosThemePathManager {

  private IosThemePathManager() {
  }

  private static Path getBasePath() {
    String os = System.getProperty("os.name");
    if (os.toLowerCase().startsWith("win")) {
      return Path.of("C:\\tmp");
    }
    return Path.of("/tmp");
  }

  public static Path createThemeWorkDir(Integer themeId) throws IOException {
    Path iosThemeRoot = getBasePath().resolve("theme").resolve("ios");
    Files.createDirectories(iosThemeRoot);
    Path workDir = iosThemeRoot.resolve(themeId + "-" + UUID.randomUUID());
    Files.createDirectories(workDir);
    return workDir;
  }

  public static Path getCssPath(Path workDir) {
    return workDir.resolve("KakaoTalkTheme.css");
  }

  public static Path getImagesDir(Path workDir) {
    return workDir.resolve("Images");
  }

  public static void deleteRecursivelyQuietly(Path path) {
    if (path == null || !Files.exists(path)) {
      return;
    }
    try (var stream = Files.walk(path)) {
      stream.sorted(Comparator.reverseOrder())
          .forEach(target -> {
            try {
              Files.deleteIfExists(target);
            } catch (IOException e) {
              log.warn("failed to delete iOS theme temp path: {}", target, e);
            }
          });
    } catch (IOException e) {
      log.warn("failed to walk iOS theme temp path: {}", path, e);
    }
  }
}
