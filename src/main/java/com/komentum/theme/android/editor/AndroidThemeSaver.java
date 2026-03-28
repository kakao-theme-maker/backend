package com.komentum.theme.android.editor;

import com.komentum.global.utils.FileManager;
import com.komentum.theme.utils.DockerProcessRunner;
import com.komentum.theme.utils.ThemePathManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidThemeSaver {

  private final DockerProcessRunner dockerProcessRunner;
  private final FileManager fileManager;

  private String resolveThemeName(String themeId) {
    return String.format("theme-%s.apk", themeId);
  }

  /**
   * docker command builder
   *
   * @param inputPath  input apk path
   * @param targetPath output path for depacked apk
   * @return docker command array like ["docker", "run", "--rm", "-v", "/input:/input", "-v",
   * "/output:/output", "louie8821/apk_repackager:test"]
   */
  public String[] commandBuilder(String inputPath, String targetPath) {
    return new String[]{
        "docker", "run", "--platform=linux/amd64", "--rm",

        "-v", String.format("%s:/input", inputPath),
        "-v", String.format("%s:/output", targetPath),
        "louie8821/apk_repackager:test",
    };
  }

  /**
   * resolve paths and depack apk on output paths
   *
   * @param inputPath  input apk path
   * @param outputPath output path for depacked apk
   */
  public byte[] repackAndSignTheme(Path inputPath, Path outputPath) throws Exception {
    String[] command = commandBuilder(
        inputPath.toAbsolutePath().toString(),
        outputPath.toAbsolutePath().toString());
    dockerProcessRunner.runDockerProcess(command);
    Path outputApk = Paths.get(outputPath.toString(), "output-signed.apk");
    return Files.readAllBytes(outputApk);
  }

  /**
   * initialize the theme on the specific theme path
   *
   * @param themeId theme id
   */
  public String repackAndSaveTheme(String themeId) throws Exception {
    Path depackedThemePath = ThemePathManager.getThemeDepackedDir(themeId).toAbsolutePath();
    Path repackedThemePath = ThemePathManager.getThemeRepackedDir(themeId).toAbsolutePath();
    byte[] outputApk = repackAndSignTheme(depackedThemePath, repackedThemePath);
    return fileManager.uploadFile(outputApk, resolveThemeName(themeId));
  }
}
