package com.komentum.theme.ios.editor;

import com.komentum.global.utils.FileManager;
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

  public String save(Integer themeComponentId, Path workDir) throws IOException {
    String fileName = resolveThemeName(themeComponentId);
    byte[] packageBytes = createPackageBytes(workDir);
    return fileManager.uploadFile(packageBytes, fileName);
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
    // 카카오톡 .ktheme 규격에 맞춰 루트의 KakaoTalkTheme.css와 Images/ 하위 파일만 포함한다.
    // 그 밖의 작업 파일과 macOS 메타데이터는 패키지에서 제외한다.
    if (relativePath.getNameCount() == 1) {
      return fileName.equals("KakaoTalkTheme.css");
    }
    return relativePath.getNameCount() > 1
        && relativePath.getName(0).toString().equals("Images");
  }

  private void addZipEntry(Path workDir, Path filePath, ZipOutputStream zipOutputStream) {
    // ZIP 엔트리 이름은 운영체제와 관계없이 표준 경로 구분자인 슬래시(/)로 통일한다.
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
