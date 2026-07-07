package com.komentum.theme.ios.editor;

import com.komentum.theme.ios.utils.IosThemePathManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class IosThemeTemplateExtractor {

  private static final String TEMPLATE_PATH = "theme/ios/templates/kakao-ios-sample.ktheme";

  private final Resource templateResource;

  public IosThemeTemplateExtractor() {
    this(new ClassPathResource(TEMPLATE_PATH));
  }

  IosThemeTemplateExtractor(Resource templateResource) {
    this.templateResource = templateResource;
  }

  public void extractTemplate(Path workDir) throws IOException {
    Files.createDirectories(workDir);
    try (InputStream inputStream = templateResource.getInputStream();
        ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        extractEntry(workDir, entry, zipInputStream);
        zipInputStream.closeEntry();
        entry = zipInputStream.getNextEntry();
      }
    }
    validateTemplate(workDir);
  }

  private void extractEntry(Path workDir, ZipEntry entry, ZipInputStream zipInputStream)
      throws IOException {
    String entryName = entry.getName();
    if (isIgnoredEntry(entryName)) {
      return;
    }
    Path normalizedWorkDir = workDir.toAbsolutePath().normalize();
    Path outputPath = normalizedWorkDir.resolve(entryName).normalize();
    if (!outputPath.startsWith(normalizedWorkDir)) {
      throw new IOException("invalid iOS theme template entry: " + entryName);
    }
    if (entry.isDirectory()) {
      Files.createDirectories(outputPath);
      return;
    }
    Files.createDirectories(outputPath.getParent());
    Files.copy(zipInputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
  }

  private boolean isIgnoredEntry(String entryName) {
    return entryName.equals(".DS_Store")
        || entryName.endsWith("/.DS_Store")
        || entryName.equals("__MACOSX")
        || entryName.startsWith("__MACOSX/")
        || entryName.contains("/__MACOSX/");
  }

  private void validateTemplate(Path workDir) throws IOException {
    Path cssPath = IosThemePathManager.getCssPath(workDir);
    if (!Files.isRegularFile(cssPath)) {
      throw new IOException("iOS theme template must contain KakaoTalkTheme.css");
    }
    Path imagesDir = IosThemePathManager.getImagesDir(workDir);
    if (!Files.isDirectory(imagesDir)) {
      throw new IOException("iOS theme template must contain Images directory");
    }
    try (var imagePaths = Files.list(imagesDir)) {
      boolean hasPng = imagePaths
          .anyMatch(path -> Files.isRegularFile(path)
              && path.getFileName().toString().endsWith(".png"));
      if (!hasPng) {
        throw new IOException("iOS theme template must contain at least one image");
      }
    }
  }
}
