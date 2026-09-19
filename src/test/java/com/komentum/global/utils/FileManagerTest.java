package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
  @DisplayName("fileName을 지정하면 그대로 업로드하고 해당 fileName을 반환한다")
  void uploadAndGetFileName_usesSpecifiedFileName() {
    String result = fileManager.uploadAndGetFileName(new byte[]{1}, "ios-theme-1.ktheme", "ktheme");

    assertThat(result).isEqualTo("ios-theme-1.ktheme");
    verify(fileManager).uploadFile(any(byte[].class), eq("ios-theme-1.ktheme"));
  }

  @Test
  @DisplayName("fileName을 지정하지 않으면 UUID + 확장자 형식으로 생성하여 업로드한다")
  void uploadAndGetFileName_generatesUuidFileNameWithExtension() {
    InputStream is = new ByteArrayInputStream(new byte[]{1});

    String result = fileManager.uploadAndGetFileName(is, 1L, null, "apk");

    assertThat(result).endsWith(".apk");
    assertThat(UUID.fromString(result.substring(0, result.length() - ".apk".length())))
        .isNotNull();
    verify(fileManager).uploadFile(eq(is), eq(1L), eq(result));
  }

  @Test
  @DisplayName("확장자는 점(.) 포함 여부와 관계없이 동일한 형식으로 생성된다")
  void uploadAndGetFileName_normalizesLeadingDotOfExtension() {
    String result = fileManager.uploadAndGetFileName(new byte[]{1}, " ", ".apk");

    assertThat(result).endsWith(".apk").doesNotContain("..");
  }

  @Test
  @DisplayName("지정한 fileName에 확장자가 없으면 확장자를 덧붙여 업로드한다")
  void uploadAndGetFileName_appendsExtensionWhenSpecifiedFileNameHasNone() {
    String result = fileManager.uploadAndGetFileName(new byte[]{1}, "ios-theme-1", "ktheme");

    assertThat(result).isEqualTo("ios-theme-1.ktheme");
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    verify(fileManager).uploadFile(any(byte[].class), fileNameCaptor.capture());
    assertThat(fileNameCaptor.getValue()).isEqualTo("ios-theme-1.ktheme");
  }

  @Test
  @DisplayName("지정한 fileName이 이미 해당 확장자로 끝나면 중복해서 붙이지 않는다")
  void uploadAndGetFileName_doesNotDuplicateExtension() {
    String result = fileManager.uploadAndGetFileName(new byte[]{1}, "theme.APK", "apk");

    assertThat(result).isEqualTo("theme.APK");
  }

  @Test
  @DisplayName("확장자가 없으면 fileName 지정 여부와 관계없이 업로드하지 않고 예외가 발생한다")
  void uploadAndGetFileName_failsWhenExtensionIsEmpty() {
    assertThatThrownBy(() -> fileManager.uploadAndGetFileName(new byte[]{1}, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> fileManager.uploadAndGetFileName(new byte[]{1}, "theme.apk", " "))
        .isInstanceOf(IllegalArgumentException.class);

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
    verify(fileManager, never()).uploadFile(any(InputStream.class), anyLong(), anyString());
  }
}
