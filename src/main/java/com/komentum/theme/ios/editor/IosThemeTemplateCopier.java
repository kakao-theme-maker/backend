package com.komentum.theme.ios.editor;

import com.komentum.theme.ios.utils.IosThemePathManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IosThemeTemplateCopier {

  private static final String TEMPLATE_CSS_PATH = "theme/ios/sample/KakaoTalkTheme.css";
  private static final String TEMPLATE_IMAGE_PATTERN = "classpath:theme/ios/sample/Images/*.png";

  private final PathMatchingResourcePatternResolver resourceResolver =
      new PathMatchingResourcePatternResolver();

  public void copyTemplate(Path workDir) throws IOException {
    copyCss(workDir);
    copyImages(workDir);
  }

  private void copyCss(Path workDir) throws IOException {
    ClassPathResource cssResource = new ClassPathResource(TEMPLATE_CSS_PATH);
    try (InputStream inputStream = cssResource.getInputStream()) {
      Files.copy(inputStream, IosThemePathManager.getCssPath(workDir),
          StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void copyImages(Path workDir) throws IOException {
    Path imagesDir = IosThemePathManager.getImagesDir(workDir);
    Files.createDirectories(imagesDir);
    Resource[] resources = resourceResolver.getResources(TEMPLATE_IMAGE_PATTERN);
    for (Resource resource : resources) {
      String fileName = resource.getFilename();
      if (fileName == null || !fileName.endsWith(".png")) {
        continue;
      }
      try (InputStream inputStream = resource.getInputStream()) {
        Files.copy(inputStream, imagesDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }
}
