package com.komentum.theme.android.editor;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.NinePatchConverter;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.android.utils.ThemePathManager;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidThemeImageEditor {

  private final FileManager fileManager;

  private static final int IMAGE_EDITOR_THREAD_POOL_SIZE = 8;
  private final ExecutorService executorService = Executors.newFixedThreadPool(
      IMAGE_EDITOR_THREAD_POOL_SIZE);

  @PreDestroy
  public void shutdown() {
    executorService.shutdown();
  }

  /**
   * edit an image on the specific theme path
   *
   * @param themeId   theme id
   * @param component theme's component info
   */
  private void editImage(String themeId, AndroidComponentDto component) throws IOException {
    // 이미지 복사를 위한 임시 파일 생성
    Path imagePath = ThemePathManager.getAndroidThemeImagePath(themeId, component);
    Path tempImagePath = imagePath.resolveSibling(imagePath.getFileName() + ".temp");
    Files.createDirectories(tempImagePath.getParent());
    // 이미지 다운로드 후 임시 파일에 복사
    String imageFileName = fileManager.convertUrlToFileName(component.getImageUrl());
    try (
        InputStream is = NinePatchConverter.convertIfNeeded(fileManager.download(imageFileName),
            component.getFileExtension());
        OutputStream os = Files.newOutputStream(tempImagePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)
    ) {
      is.transferTo(os);
    }
    // 임시 파일 데이터를 실제 이미지에 복사
    Files.move(
        tempImagePath,
        imagePath,
        StandardCopyOption.REPLACE_EXISTING
    );
  }

  /**
   * edit all images on the specific theme path by the component list
   *
   * @param themeId    theme id
   * @param components theme's component info list
   */
  public void editImages(String themeId, List<AndroidComponentDto> components) {
    List<CompletableFuture<Void>> futures = components.stream()
        .map(component -> CompletableFuture.runAsync(() -> {
          try {
            editImage(themeId, component);
          } catch (IOException e) {
            log.error(
                "[AndroidThemeImageEditor] Failed to save image on theme: {}",
                component.getImageFilePath(),
                e
            );
            throw new UncheckedIOException(e);
          }
        }, executorService)).toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }
}
