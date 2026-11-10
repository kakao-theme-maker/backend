package com.komentum.theme.ios.editor;

import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.ImageUtils;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.ios.utils.IosThemePathManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IosThemeImageEditor {

  private final FileManager fileManager;

  public void editImages(
      Path workDir,
      List<ThemeImage> themeImages,
      List<PlatformComponentType> platformComponentTypes
  ) throws IOException {
    if (platformComponentTypes == null || platformComponentTypes.isEmpty()) {
      throw new IllegalStateException("iOS platform component type seed is required");
    }
    Map<TypeCode, ThemeImage> themeImageMap = themeImages.stream()
        .collect(Collectors.toMap(
            themeImage -> themeImage.getComponentType().getTypeCode(),
            themeImage -> themeImage,
            (left, right) -> left
        ));
    Map<TypeCode, List<PlatformComponentType>> platformTypeMap = platformComponentTypes.stream()
        .collect(Collectors.groupingBy(pct -> pct.getComponentType().getTypeCode()));

    // 하나의 ThemeImage로 같은 TypeCode에 매핑된 @2x/@3x 및 상태별 iOS 파일을
    // 모두 생성한다. 사용자 이미지 매핑이나 다운로드 데이터가 없으면 파일을
    // 덮어쓰지 않아 템플릿 기본 이미지를 유지한다.
    for (Map.Entry<TypeCode, List<PlatformComponentType>> entry : platformTypeMap.entrySet()) {
      ThemeImage themeImage = themeImageMap.get(entry.getKey());
      if (themeImage == null || themeImage.getDesignComponent() == null) {
        continue;
      }
      BufferedImage source = readThemeImage(themeImage);
      if (source == null) {
        continue;
      }
      for (PlatformComponentType platformComponentType : entry.getValue()) {
        writeVariant(workDir, entry.getKey(), source, platformComponentType);
      }
    }
  }

  private BufferedImage readThemeImage(ThemeImage themeImage) throws IOException {
    String fileName = themeImage.getDesignComponent().getFileName();
    if (fileName == null || fileName.isBlank()) {
      return null;
    }
    byte[] imageBytes = fileManager.downloadFile(fileName);
    if (imageBytes == null || imageBytes.length == 0) {
      return null;
    }
    BufferedImage source = ImageIO.read(new ByteArrayInputStream(imageBytes));
    if (source == null) {
      throw new IOException("failed to read theme image: " + fileName);
    }
    return source;
  }

  private void writeVariant(
      Path workDir,
      TypeCode typeCode,
      BufferedImage source,
      PlatformComponentType platformComponentType
  ) throws IOException {
    // 대상 영역 전체를 채워야 하는 배경 이미지는 투명 여백이 생기지 않도록 중앙 크롭한다.
    // 나머지 아이콘과 장식 이미지는 원본 비율을 유지한 채 투명 여백으로 크기를 맞춘다.
    BufferedImage resized = typeCode.isBackgroundImage()
        ? ImageUtils.scaleDownToCover(source, platformComponentType.getWidth(),
        platformComponentType.getHeight())
        : ImageUtils.resizeFit(source, platformComponentType.getWidth(),
            platformComponentType.getHeight());
    Path outputPath = IosThemePathManager.getImagesDir(workDir)
        .resolve(platformComponentType.getPath());
    Files.createDirectories(outputPath.getParent());
    ImageIO.write(resized, platformComponentType.getFileExtension().getExtension(),
        outputPath.toFile());
  }
}
