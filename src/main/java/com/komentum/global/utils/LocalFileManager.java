package com.komentum.global.utils;

import com.komentum.config.WebConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "file.storage", havingValue = "local")
public class LocalFileManager implements FileManager {

  @Value("${file.base-url}")
  private String baseUrl;

  @PostConstruct
  public void init() throws IOException {
    Path uploadPath = Paths.get(WebConfig.UPLOAD_DIR);
    Files.createDirectories(uploadPath);
  }

  @PreDestroy
  public void tearDown() throws IOException {
    FileUtils.cleanDirectory(new File(WebConfig.UPLOAD_DIR));
  }

  private String resolveFileLocation(String fileName) {
    return WebConfig.UPLOAD_DIR + "/" + fileName;
  }

  private String resolveFilePathPrefix() {
    String normalizedBaseUrl = StringUtils.removeTrailingSlash(baseUrl);
    String normalizedUploadUrlPrefix = StringUtils.trimSlash(WebConfig.UPLOAD_URL_PREFIX);
    return normalizedBaseUrl + "/" + normalizedUploadUrlPrefix + "/";
  }

  @Override
  public String resolveFilePath(String fileName) {
    return resolveFilePathPrefix() + fileName;
  }

  @Override
  public String convertUrlToFileName(String fileUrl) {
    if (fileUrl == null) {
      throw new IllegalArgumentException("failed to convert url to file name : file url is null");
    }
    String fileUrlPrefix = resolveFilePathPrefix();
    if (fileUrl.startsWith(fileUrlPrefix)) {
      return fileUrl.substring(fileUrlPrefix.length());
    }
    throw new IllegalArgumentException(
        "failed to convert url to file name : " + fileUrl + " doesn't start with " + fileUrlPrefix);
  }

  @Override
  public String uploadFile(byte[] fileBytes, String fileName) {
    String fileLocation = resolveFileLocation(fileName);
    try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
      Files.copy(inputStream, Paths.get(fileLocation));
    } catch (IOException e) {
      throw new RuntimeException("failed to upload file : " + fileLocation, e);
    }
    return resolveFilePath(fileName);
  }

  @Override
  public void deleteFile(String fileName) {
    String fileLocation = resolveFileLocation(fileName);
    try {
      Files.deleteIfExists(Paths.get(fileLocation));
    } catch (IOException e) {
      throw new RuntimeException("failed to delete file : " + fileLocation, e);
    }
  }

  @Override
  public byte[] downloadFile(String fileName) {
    String fileLocation = resolveFileLocation(fileName);
    try {
      return Files.readAllBytes(Paths.get(fileLocation));
    } catch (IOException e) {
      throw new RuntimeException("failed to read file : " + fileName, e);
    }
  }
}
