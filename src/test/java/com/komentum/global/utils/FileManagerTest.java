package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileManagerTest {

  @Mock(answer = Answers.CALLS_REAL_METHODS)
  private FileManager fileManager;

  @Test
  @DisplayName("기본 구현은 테마 패키지 다운로드에 기존 고정 URL을 반환한다")
  void createThemePackageDownloadUrl_returnsPublicFileUrl() {
    given(fileManager.resolvePublicFileUrl("theme.apk"))
        .willReturn("https://example.com/theme.apk");

    String result = fileManager.createThemePackageDownloadUrl("theme.apk");

    assertThat(result).isEqualTo("https://example.com/theme.apk");
    verify(fileManager).resolvePublicFileUrl("theme.apk");
  }

  @Test
  @DisplayName("fileName을 지정하면 그대로 업로드하고 해당 fileName을 반환한다")
  void uploadThemePackageAndGetFileName_usesSpecifiedFileName() {
    String result = fileManager.uploadThemePackageAndGetFileName(
        new byte[]{1}, "ios-theme-1.ktheme", "ktheme");

    assertThat(result).isEqualTo("ios-theme-1.ktheme");
    verify(fileManager).uploadPublicFile(any(byte[].class), eq("ios-theme-1.ktheme"));
  }

  @Test
  @DisplayName("fileName을 지정하지 않으면 UUID + 확장자 형식으로 생성하여 업로드한다")
  void uploadThemePackageAndGetFileName_generatesUuidFileNameWithExtension() {
    InputStream is = new ByteArrayInputStream(new byte[]{1});

    String result = fileManager.uploadThemePackageAndGetFileName(is, 1L, null, "apk");

    assertThat(result).endsWith(".apk");
    assertThat(UUID.fromString(result.substring(0, result.length() - ".apk".length())))
        .isNotNull();
    verify(fileManager).uploadPublicFile(eq(is), eq(1L), eq(result));
  }

  @Test
  @DisplayName("확장자는 점(.) 포함 여부와 관계없이 동일한 형식으로 생성된다")
  void uploadThemePackageAndGetFileName_normalizesLeadingDotOfExtension() {
    String result = fileManager.uploadThemePackageAndGetFileName(new byte[]{1}, " ", ".apk");

    assertThat(result).endsWith(".apk").doesNotContain("..");
  }

  @Test
  @DisplayName("지정한 fileName에 확장자가 없으면 확장자를 덧붙여 업로드한다")
  void uploadThemePackageAndGetFileName_appendsExtensionWhenSpecifiedFileNameHasNone() {
    String result = fileManager.uploadThemePackageAndGetFileName(new byte[]{1}, "ios-theme-1", "ktheme");

    assertThat(result).isEqualTo("ios-theme-1.ktheme");
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    verify(fileManager).uploadPublicFile(any(byte[].class), fileNameCaptor.capture());
    assertThat(fileNameCaptor.getValue()).isEqualTo("ios-theme-1.ktheme");
  }

  @Test
  @DisplayName("지정한 fileName이 이미 해당 확장자로 끝나면 중복해서 붙이지 않는다")
  void uploadThemePackageAndGetFileName_doesNotDuplicateExtension() {
    String result = fileManager.uploadThemePackageAndGetFileName(new byte[]{1}, "theme.APK", "apk");

    assertThat(result).isEqualTo("theme.APK");
  }

  @Test
  @DisplayName("확장자가 없으면 fileName 지정 여부와 관계없이 업로드하지 않고 예외가 발생한다")
  void uploadThemePackageAndGetFileName_failsWhenExtensionIsEmpty() {
    assertThatThrownBy(() -> fileManager.uploadThemePackageAndGetFileName(new byte[]{1}, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
        () -> fileManager.uploadThemePackageAndGetFileName(new byte[]{1}, "theme.apk", " "))
        .isInstanceOf(IllegalArgumentException.class);

    verify(fileManager, never()).uploadPublicFile(any(byte[].class), anyString());
    verify(fileManager, never()).uploadPublicFile(any(InputStream.class), anyLong(), anyString());
  }
}
