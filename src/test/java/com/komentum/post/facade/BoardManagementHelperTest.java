package com.komentum.post.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardManagementHelperTest {

  @Mock
  private FileManager fileManager;

  @Mock
  private PostService postService;

  private BoardManagementHelper boardManagementHelper;

  @BeforeEach
  void setUp() {
    boardManagementHelper = new BoardManagementHelper(fileManager, postService);
  }

  @Test
  @DisplayName("업로드와 정리 삭제가 모두 실패하면 최초 업로드 오류를 원인으로 보존한다")
  void savePreviewImageIfPresent_uploadAndCleanupFail_preservesUploadFailure() {
    RuntimeException uploadFailure = new RuntimeException("forced upload failure");
    RuntimeException cleanupFailure = new RuntimeException("forced cleanup failure");
    given(fileManager.uploadFile(any(byte[].class), anyString())).willThrow(uploadFailure);
    willThrow(cleanupFailure).given(fileManager).deleteFile(anyString());

    Throwable thrown = catchThrowable(
        () -> boardManagementHelper.savePreviewImageIfPresent(
            Post.class, "preview.png", "preview-image".getBytes()));

    assertThat(thrown)
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Failed to process preview image file")
        .hasCause(uploadFailure);
    assertThat(thrown.getCause().getSuppressed()).containsExactly(cleanupFailure);
  }
}
