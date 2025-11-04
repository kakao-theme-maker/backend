package com.komentum.theme.android.editor;

import com.komentum.global.utils.FileManager;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.utils.ThemePathManager;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  private static final int IMAGE_EDITOR_THREAD_POOL_SIZE = 4;

  /**
   * edit an image on the specific theme path
   *
   * @param themeId   theme id
   * @param component theme's component info
   */
  public void editImage(String themeId, AndroidComponentDto component) throws IOException {
    Path imagePath = ThemePathManager.getImagePath(themeId, component);
    byte[] imageBytes = fileManager.downloadFile(component.getImageUrl());
    Path tempPath = Paths.get(imagePath + ".tmp");
    // Ensure parent directory exists
    Files.createDirectories(tempPath.getParent());
    try (OutputStream os = Files.newOutputStream(tempPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING)) {
      os.write(imageBytes);
      os.flush();
    }
    Files.move(tempPath, imagePath, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * edit all images on the specific theme path by the component list
   *
   * @param themeId    theme id
   * @param components theme's component info list
   */
  public void editImages(String themeId, List<AndroidComponentDto> components) {
    ExecutorService executorService = Executors.newFixedThreadPool(IMAGE_EDITOR_THREAD_POOL_SIZE);
    List<CompletableFuture<Void>> futures = components.stream()
        .map(component -> CompletableFuture.runAsync(() -> {
          try {
            editImage(themeId, component);
          } catch (IOException e) {
            log.error(e.getMessage());
            log.error("[ AndroidThemeImageEditor ] failed to edit image on {}",
                component.getAndroidComponentPath());
            throw new RuntimeException(e);
          }
        }, executorService)).toList();
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    executorService.shutdown();
  }
}
