package com.komentum.theme.ios.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.when;

import com.komentum.global.utils.FileManager;
import com.komentum.theme.ios.dto.IosThemePackageResponse;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class IosThemeSaverTest {

  @TempDir
  Path tempDir;

  @Test
  void save_uploadsKthemeWithCssAndImagesOnly() throws Exception {
    // given
    Files.createDirectories(tempDir.resolve("Images"));
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), "css", StandardCharsets.UTF_8);
    Files.writeString(tempDir.resolve("unexpected.txt"), "ignored", StandardCharsets.UTF_8);
    Files.write(tempDir.resolve("Images/sample.png"), new byte[]{1, 2, 3});
    Files.writeString(tempDir.resolve(".DS_Store"), "ignored", StandardCharsets.UTF_8);
    FileManager fileManager = Mockito.mock(FileManager.class);
    when(fileManager.uploadFile(any(byte[].class), endsWith(".ktheme")))
        .thenReturn("https://cdn.example.com/theme.ktheme");
    IosThemeSaver saver = new IosThemeSaver(fileManager);

    // when
    IosThemePackageResponse response = saver.save(3, tempDir);

    // then
    ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
    Mockito.verify(fileManager).uploadFile(captor.capture(), endsWith(".ktheme"));
    assertThat(response.getThemeComponentId()).isEqualTo(3);
    assertThat(response.getThemeUrl()).isEqualTo("https://cdn.example.com/theme.ktheme");
    assertThat(response.getFileName()).startsWith("ios-theme-3-").endsWith(".ktheme");
    assertThat(readZipEntries(captor.getValue()))
        .containsExactly("Images/sample.png", "KakaoTalkTheme.css");
  }

  private List<String> readZipEntries(byte[] zipBytes) throws Exception {
    List<String> entries = new ArrayList<>();
    try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
      var entry = zipInputStream.getNextEntry();
      while (entry != null) {
        entries.add(entry.getName());
        entry = zipInputStream.getNextEntry();
      }
    }
    return entries;
  }
}
