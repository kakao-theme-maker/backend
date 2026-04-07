package com.komentum.global.utils;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileUtils {

  private final FileManager fileManager;


  public <T> String generateUniqueFileName(Class<T> entity, String extension) {
    return entity.getName()
        + "_" + UUID.randomUUID()
        + "_" + System.currentTimeMillis()
        + "." + extension;
  }

  public String extractExtension(String originFileName) {
    String extension = StringUtils.getFilenameExtension(originFileName);
    if (extension == null || extension.isEmpty()) {
      extension = "bin";
    }
    return extension;
  }

  public void deleteFileSilently(String fileName, String errorMessage) {
    try {
      if (fileName != null) {
        fileManager.deleteFile(fileName);
      }
    } catch (Exception e) {
      log.warn(errorMessage);
    }
  }
}
