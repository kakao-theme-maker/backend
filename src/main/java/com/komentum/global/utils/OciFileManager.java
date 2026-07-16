package com.komentum.global.utils;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage", havingValue = "oci")
public class OciFileManager implements FileManager {

  private final ObjectStorage objectStorage;
  private final OciObjectStorageProperty property;

  private String resolveFilePathPrefix() {
    return StringUtils.removeTrailingSlash(property.getParBaseUrl()) + "/";
  }

  private void validateFileName(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("[OCI File Manager] fileName is null or empty");
    }
  }

  @Override
  public String resolveFilePath(String fileName) {
    validateFileName(fileName);
    return resolveFilePathPrefix() + UriUtils.encodePath(fileName, StandardCharsets.UTF_8);
  }

  @Override
  public String convertUrlToFileName(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      throw new IllegalArgumentException("failed to convert url to file name : file url is null");
    }
    String fileUrlPrefix = resolveFilePathPrefix();
    if (!fileUrl.startsWith(fileUrlPrefix)) {
      throw new IllegalArgumentException(
          "failed to convert url to file name : file URL doesn't match OCI PAR prefix");
    }
    String encodedFileName = fileUrl.substring(fileUrlPrefix.length());
    if (encodedFileName.isBlank()) {
      throw new IllegalArgumentException("failed to convert url to file name : file name is empty");
    }
    return UriUtils.decode(encodedFileName, StandardCharsets.UTF_8);
  }

  @Override
  public String uploadFile(byte[] fileBytes, String fileName) {
    Objects.requireNonNull(fileBytes, "fileBytes is null");
    return uploadFile(new ByteArrayInputStream(fileBytes), fileBytes.length, fileName);
  }

  @Override
  public String uploadFile(InputStream is, long contentLength, String fileName) {
    Objects.requireNonNull(is, "inputStream is null");
    validateFileName(fileName);
    PutObjectRequest request = PutObjectRequest.builder()
        .namespaceName(property.getNamespace())
        .bucketName(property.getBucketName())
        .objectName(fileName)
        .contentLength(contentLength)
        .putObjectBody(is)
        .build();
    try (is) {
      objectStorage.putObject(request);
    } catch (IOException e) {
      throw new UncheckedIOException("failed to close upload stream : " + fileName, e);
    }
    return resolveFilePath(fileName);
  }

  @Override
  public void deleteFile(String fileName) {
    validateFileName(fileName);
    DeleteObjectRequest request = DeleteObjectRequest.builder()
        .namespaceName(property.getNamespace())
        .bucketName(property.getBucketName())
        .objectName(fileName)
        .build();
    objectStorage.deleteObject(request);
  }

  @Override
  public byte[] downloadFile(String fileName) {
    try (InputStream inputStream = download(fileName)) {
      return inputStream.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("failed to read file : " + fileName, e);
    }
  }

  @Override
  public InputStream download(String fileName) {
    validateFileName(fileName);
    GetObjectRequest request = GetObjectRequest.builder()
        .namespaceName(property.getNamespace())
        .bucketName(property.getBucketName())
        .objectName(fileName)
        .build();
    return objectStorage.getObject(request).getInputStream();
  }
}
