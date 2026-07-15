package com.komentum.theme.ios.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;

class IosThemeTemplateExtractorTest {

  @TempDir
  Path tempDir;

  @Test
  void extractTemplate_extractsCssAndImages() throws Exception {
    // given
    Path templatePath = createTemplate(Map.of(
        "KakaoTalkTheme.css", "css".getBytes(StandardCharsets.UTF_8),
        "Images/sample.png", new byte[]{1, 2, 3},
        ".DS_Store", "ignored".getBytes(StandardCharsets.UTF_8),
        "__MACOSX/sample.png", "ignored".getBytes(StandardCharsets.UTF_8)
    ));
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));
    Path workDir = tempDir.resolve("work");

    // when
    extractor.extractTemplate(workDir);

    // then
    assertThat(workDir.resolve("KakaoTalkTheme.css")).isRegularFile();
    assertThat(workDir.resolve("Images/sample.png")).isRegularFile();
    assertThat(workDir.resolve(".DS_Store")).doesNotExist();
    assertThat(workDir.resolve("__MACOSX")).doesNotExist();
  }

  @Test
  void extractTemplate_extractsBundledTemplateWithAllCssImageReferences() throws Exception {
    // given
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor();
    Path workDir = tempDir.resolve("work");

    // when
    extractor.extractTemplate(workDir);

    // then
    assertThat(workDir.resolve("KakaoTalkTheme.css")).isRegularFile();
    assertThat(workDir.resolve("Images/maintabIcoPiccoma@2x.png")).isRegularFile();
    assertThat(workDir.resolve("Images/maintabIcoPiccomaSelected@3x.png")).isRegularFile();
  }

  @Test
  void extractTemplate_allowsScaledImageForCssReference() throws Exception {
    // given
    Path templatePath = createTemplate(Map.of(
        "KakaoTalkTheme.css", "-ios-image: 'foo.png';".getBytes(StandardCharsets.UTF_8),
        "Images/foo@2x.png", new byte[]{1, 2, 3}
    ));
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));

    // when
    extractor.extractTemplate(tempDir.resolve("work"));

    // then
    assertThat(tempDir.resolve("work/Images/foo@2x.png")).isRegularFile();
  }

  @Test
  void extractTemplate_throwsWhenCssReferencesMissingImage() throws Exception {
    // given
    Path templatePath = createTemplate(Map.of(
        "KakaoTalkTheme.css", "-ios-image: 'missing.png';".getBytes(StandardCharsets.UTF_8),
        "Images/sample.png", new byte[]{1, 2, 3}
    ));
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));

    // when & then
    assertThatThrownBy(() -> extractor.extractTemplate(tempDir.resolve("work")))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("missing.png");
  }

  @Test
  void extractTemplate_throwsWhenEntryEscapesWorkDir() throws Exception {
    // given
    Path templatePath = createTemplate(Map.of(
        "KakaoTalkTheme.css", "css".getBytes(StandardCharsets.UTF_8),
        "Images/sample.png", new byte[]{1, 2, 3},
        "../evil.txt", "evil".getBytes(StandardCharsets.UTF_8)
    ));
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));
    Path workDir = tempDir.resolve("work");

    // when & then
    assertThatThrownBy(() -> extractor.extractTemplate(workDir))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("invalid iOS theme template entry");
    assertThat(tempDir.resolve("evil.txt")).doesNotExist();
  }

  @Test
  void extractTemplate_throwsWhenCssIsMissing() throws Exception {
    // given
    Path templatePath = createTemplate(Map.of(
        "Images/sample.png", new byte[]{1, 2, 3}
    ));
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));

    // when & then
    assertThatThrownBy(() -> extractor.extractTemplate(tempDir.resolve("work")))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("KakaoTalkTheme.css");
  }

  @Test
  void extractTemplate_throwsWhenImagesDirectoryIsMissing() throws Exception {
    // given
    Path templatePath = createTemplate(Map.of(
        "KakaoTalkTheme.css", "css".getBytes(StandardCharsets.UTF_8)
    ));
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));

    // when & then
    assertThatThrownBy(() -> extractor.extractTemplate(tempDir.resolve("work")))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("Images directory");
  }

  @Test
  void extractTemplate_throwsWhenImagesDirectoryHasNoPng() throws Exception {
    // given
    Map<String, byte[]> entries = new LinkedHashMap<>();
    entries.put("KakaoTalkTheme.css", "css".getBytes(StandardCharsets.UTF_8));
    entries.put("Images/", null);
    entries.put("Images/readme.txt", "text".getBytes(StandardCharsets.UTF_8));
    Path templatePath = createTemplate(entries);
    IosThemeTemplateExtractor extractor = new IosThemeTemplateExtractor(
        new FileSystemResource(templatePath));

    // when & then
    assertThatThrownBy(() -> extractor.extractTemplate(tempDir.resolve("work")))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("at least one image");
  }

  private Path createTemplate(Map<String, byte[]> entries) throws IOException {
    Path templatePath = Files.createTempFile(tempDir, "ios-template-", ".ktheme");
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(
        Files.newOutputStream(templatePath))) {
      for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
        zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
        if (entry.getValue() != null) {
          zipOutputStream.write(entry.getValue());
        }
        zipOutputStream.closeEntry();
      }
    }
    return templatePath;
  }
}
