package com.komentum.post.facade;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.FileUtils;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardManagementHelper {

  private final FileManager fileManager;
  private final FileUtils fileUtils;

  public <T> String savePreviewImageIfPresent(Class<T> entity, MultipartFile previewImage) {
    if (previewImage == null || previewImage.isEmpty()) {
      return null;
    }
    try {
      return savePreviewImageIfPresent(entity, previewImage.getOriginalFilename(),
          previewImage.getBytes());
    } catch (IOException e) {
      throw new RuntimeException("failed to convert multipart file to bytes");
    }
  }

  public <T> String savePreviewImageIfPresent(Class<T> entity, String originFileName,
      byte[] previewImage) {
    if (previewImage == null || originFileName == null || originFileName.isEmpty()) {
      return null;
    }
    String extension = fileUtils.extractExtension(originFileName);
    String previewImageFileName = fileUtils.generateUniqueFileName(entity, extension);
    try {
      String imageUrl = fileManager.uploadFile(previewImage, previewImageFileName);
      if (imageUrl == null) {
        throw new RuntimeException("Failed to upload preview image file");
      }
      return previewImageFileName;
    } catch (Exception e) {
      fileManager.deleteFile(previewImageFileName);
      throw new RuntimeException("Failed to process preview image file", e);
    }
  }

  public String findPreviewImageUrl(String fileName) {
    if (fileName == null) {
      return null;
    }
    return fileManager.resolveFilePath(fileName);
  }

}
