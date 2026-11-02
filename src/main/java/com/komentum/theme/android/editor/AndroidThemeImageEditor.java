package com.komentum.theme.android.editor;

import com.komentum.global.enums.FileExtension;
import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.ImageUtils;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.android.utils.NinePatchConverter;
import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.core.domain.ImageInset;
import jakarta.annotation.PreDestroy;
import java.awt.image.BufferedImage;
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
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidThemeImageEditor {

  private static final int IMAGE_EDITOR_THREAD_POOL_SIZE = 8;
  private final FileManager fileManager;
  private final ExecutorService executorService = Executors.newFixedThreadPool(
      IMAGE_EDITOR_THREAD_POOL_SIZE);
  private final ThemePathManager themePathManager;

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
  private void editImage(String themeId, String buildIdentifier, AndroidComponentDto component)
      throws IOException {
    // 이미지 복사를 위한 임시 파일 생성
    Path imagePath = themePathManager.getAndroidThemeImagePath(buildIdentifier, component);
    Path tempImagePath = imagePath.resolveSibling(imagePath.getFileName() + ".temp");
    Files.createDirectories(tempImagePath.getParent());
    // 이미지 다운로드 후 스펙 크기로 리사이즈하고, 9-patch로 변환해 임시 파일에 저장
    BufferedImage source;
    try (InputStream is = fileManager.download(component.getImageFileName())) {
      source = ImageIO.read(is);
    }
    if (source == null) {
      throw new IOException(
          "[AndroidThemeImageEditor] Invalid source image : " + component.getImageFileName());
    }
    BufferedImage result = resizeAndConvert(source, component);
    try (OutputStream os = Files.newOutputStream(tempImagePath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING)) {
      ImageIO.write(result, FileExtension.PNG.getExtension(), os);
    }
    // 임시 파일 데이터를 실제 이미지에 복사
    Files.move(
        tempImagePath,
        imagePath,
        StandardCopyOption.REPLACE_EXISTING
    );
  }

  /**
   * 원본 이미지를 플랫폼 스펙 크기(sizeX x sizeY)에 맞게 리사이즈한 뒤 필요하면 9-patch로 변환한다.
   * <ul>
   *   <li>inset이 있는 9-patch(말풍선): inset 좌표가 어긋나지 않도록 여백 없이 비율 유지 축소만 하고, inset도 같은 비율로 변환한다</li>
   *   <li>배경 이미지: 기기 화면 비율에 맞춘 크롭은 Android가 수행하므로, 자르지 않고 스펙을 덮는 크기까지 비율 유지 축소만 한다</li>
   *   <li>그 외 이미지: 스펙 크기에 정확히 맞추고, 비율이 다르면 투명 여백으로 채운다</li>
   * </ul>
   *
   * @throws IllegalStateException 스펙 크기 정보가 없는 경우
   */
  private BufferedImage resizeAndConvert(BufferedImage source, AndroidComponentDto component) {
    Integer width = component.getSizeX();
    Integer height = component.getSizeY();
    // AndroidComponentDto의 값(PlatformComponentType의 width와 height)의 값을 검증한다
    if (width == null || height == null || width <= 0 || height <= 0) {
      throw new IllegalStateException(
          "[AndroidThemeImageEditor] invalid image size spec : " + component.getImageFilePath());
    }
    // inset정보가 있는 9-patch 이미지라면, inset과 이미지 크기를 조정하고 필요하며 9-patch 이미지로 변환하여 반환한다
    ImageInset imageInset = component.getImageInset();
    if (component.getFileExtension() == FileExtension.NINE_PATCH_PNG && imageInset != null) {
      BufferedImage resized = ImageUtils.scaleDownToFit(source, width, height);
      ImageInset resizedInset = imageInset.scale(source.getWidth(), source.getHeight(),
          resized.getWidth(), resized.getHeight());
      return NinePatchConverter.convertIfNeeded(resized, component.getFileExtension(),
          resizedInset);
    }
    // 배경 이미지라면 실제 이미지 크기보다 큰 가장 작은 크기로 줄이고, 그렇지 않다면 실제 이미지보다 작은 가장 작은 크기로 줄인다 ( 배경 해상도 문제 )
    BufferedImage resized =
        component.getTypeCode() != null && component.getTypeCode().isBackgroundImage()
            ? ImageUtils.scaleDownToCover(source, width, height)
            : ImageUtils.resizeFit(source, width, height);
    return NinePatchConverter.convertIfNeeded(resized, component.getFileExtension(), null);
  }

  /**
   * edit all images on the specific theme path by the component list
   *
   * @param themeId    theme id
   * @param components theme's component info list
   */
  public void editImages(String themeId, String buildIdentifier,
      List<AndroidComponentDto> components) {
    List<CompletableFuture<Void>> futures = components.stream()
        .map(component -> CompletableFuture.runAsync(() -> {
          try {
            editImage(themeId, buildIdentifier, component);
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
