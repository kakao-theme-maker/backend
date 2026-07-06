package com.komentum.theme.ios.editor;

import com.komentum.global.utils.FileManager;
import com.komentum.theme.ios.dto.IosThemePackageResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IosThemeSaver {

  private final FileManager fileManager;

  public IosThemePackageResponse save(Integer themeComponentId, Path workDir) throws IOException {
    String fileName = resolveThemeName(themeComponentId);
    byte[] packageBytes = createPackageBytes(workDir);
    String themeUrl = fileManager.uploadFile(packageBytes, fileName);
    return IosThemePackageResponse.builder()
        .themeComponentId(themeComponentId)
        .platform(com.komentum.designcomponent.enums.Platform.IOS)
        .fileName(fileName)
        .themeUrl(themeUrl)
        .build();
  }

  private String resolveThemeName(Integer themeComponentId) {
    return "ios-theme-" + themeComponentId + "-" + UUID.randomUUID() + ".ktheme";
  }

  private byte[] createPackageBytes(Path workDir) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        var paths = Files.walk(workDir)) {
      paths
          .filter(Files::isRegularFile)
          .filter(path -> isPackageEntry(workDir, path))
          .sorted(Comparator.comparing(path -> workDir.relativize(path).toString()))
          .forEach(path -> addZipEntry(workDir, path, zipOutputStream));
    }
    return outputStream.toByteArray();
  }

  private boolean isPackageEntry(Path workDir, Path path) {
    Path relativePath = workDir.relativize(path);
    String fileName = path.getFileName().toString();
    if (fileName.equals(".DS_Store") || path.toString().contains("__MACOSX")) {
      return false;
    }
    if (relativePath.getNameCount() == 1) {
      return fileName.equals("KakaoTalkTheme.css");
    }
    return relativePath.getNameCount() > 1
        && relativePath.getName(0).toString().equals("Images");
  }

  private void addZipEntry(Path workDir, Path filePath, ZipOutputStream zipOutputStream) {
    String entryName = workDir.relativize(filePath).toString().replace('\\', '/');
    try {
      zipOutputStream.putNextEntry(new ZipEntry(entryName));
      Files.copy(filePath, zipOutputStream);
      zipOutputStream.closeEntry();
    } catch (IOException e) {
      throw new RuntimeException("failed to add iOS theme package entry: " + entryName, e);
    }
  }
}
