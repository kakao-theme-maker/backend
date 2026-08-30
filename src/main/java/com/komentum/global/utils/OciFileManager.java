package com.komentum.global.utils;

import com.komentum.config.WebConfig;
import com.komentum.global.properties.FileStorageProperty;
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
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage", havingValue = "oci")
public class OciFileManager implements FileManager {

  private final ObjectStorage objectStorage;
  private final OciObjectStorageProperty property;
  private final FileStorageProperty fileStorageProperty;

  /**
   * OCI 객체에 접근할 때 사용할 파일 URL 접두사를 생성한다.
   *
   * @return 정규화된 파일 URL 접두사
   */
  private String resolveFilePathPrefix() {
    String normalizedBaseUrl = StringUtils.removeTrailingSlash(fileStorageProperty.getBaseUrl());
    String normalizedUploadUrlPrefix = StringUtils.trimSlash(WebConfig.UPLOAD_URL_PREFIX);
    return normalizedBaseUrl + "/" + normalizedUploadUrlPrefix + "/";
  }

  /**
   * OCI 객체 이름으로 사용할 파일명이 유효한지 검증한다.
   *
   * @param fileName 검증할 파일명
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   */
  private void validateFileName(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("[OCI File Manager] fileName is null or empty");
    }
  }

  /**
   * OCI 객체 이름을 URL 인코딩된 파일 접근 URL로 변환한다.
   *
   * @param fileName OCI 객체 이름
   * @return 파일 접근 URL
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   */
  @Override
  public String resolveFilePath(String fileName) {
    validateFileName(fileName);
    return resolveFilePathPrefix() + UriUtils.encodePath(fileName, StandardCharsets.UTF_8);
  }

  /**
   * 파일 접근 URL에서 URL 디코딩된 OCI 객체 이름을 추출한다.
   *
   * @param fileUrl 파일 접근 URL
   * @return OCI 객체 이름
   * @throws IllegalArgumentException URL이 비어 있거나 접두사와 일치하지 않거나 객체 이름이 없는 경우
   */
  @Override
  public String convertUrlToFileName(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      throw new IllegalArgumentException("failed to convert url to file name : file url is null");
    }
    String fileUrlPrefix = resolveFilePathPrefix();
    if (!fileUrl.startsWith(fileUrlPrefix)) {
      throw new IllegalArgumentException(
          "failed to convert url to file name : file URL doesn't match file URL prefix");
    }
    String encodedFileName = fileUrl.substring(fileUrlPrefix.length());
    if (encodedFileName.isBlank()) {
      throw new IllegalArgumentException("failed to convert url to file name : file name is empty");
    }
    return UriUtils.decode(encodedFileName, StandardCharsets.UTF_8);
  }

  /**
   * 바이트 배열을 OCI Object Storage에 업로드하고 파일 접근 URL을 반환한다.
   *
   * @param fileBytes 업로드할 파일 데이터
   * @param fileName OCI 객체 이름
   * @return 업로드한 파일의 접근 URL
   * @throws NullPointerException 파일 데이터가 null인 경우
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   * @throws UncheckedIOException 업로드 스트림을 닫지 못한 경우
   */
  @Override
  public String uploadFile(byte[] fileBytes, String fileName) {
    Objects.requireNonNull(fileBytes, "fileBytes is null");
    return uploadFile(new ByteArrayInputStream(fileBytes), fileBytes.length, fileName);
  }

  /**
   * 입력 스트림을 OCI Object Storage에 업로드하고 스트림을 닫은 뒤 파일 접근 URL을 반환한다.
   *
   * @param is 업로드할 파일 입력 스트림
   * @param contentLength 파일 크기
   * @param fileName OCI 객체 이름
   * @return 업로드한 파일의 접근 URL
   * @throws NullPointerException 입력 스트림이 null인 경우
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   * @throws UncheckedIOException 업로드 스트림을 닫지 못한 경우
   */
  @Override
  public String uploadFile(InputStream is, long contentLength, String fileName) {
    Objects.requireNonNull(is, "inputStream is null");
    validateFileName(fileName);
    String lowerCaseFileName = fileName.toLowerCase(Locale.ROOT);
    String contentType = lowerCaseFileName.endsWith(".png")
        ? MediaType.IMAGE_PNG_VALUE
        : lowerCaseFileName.endsWith(".jpg") || lowerCaseFileName.endsWith(".jpeg")
            ? MediaType.IMAGE_JPEG_VALUE
            : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    try (is) {
      PutObjectRequest request = PutObjectRequest.builder()
          .namespaceName(property.getNamespace())
          .bucketName(property.getBucketName())
          .objectName(fileName)
          .contentLength(contentLength)
          .contentType(contentType)
          .putObjectBody(is)
          .build();
      objectStorage.putObject(request);
    } catch (IOException e) {
      throw new UncheckedIOException("failed to close upload stream : " + fileName, e);
    }
    return resolveFilePath(fileName);
  }

  /**
   * 파일명에 해당하는 OCI 객체를 삭제한다.
   *
   * @param fileName 삭제할 OCI 객체 이름
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   */
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

  /**
   * 파일명에 해당하는 OCI 객체를 바이트 배열로 다운로드한다.
   *
   * @param fileName 다운로드할 OCI 객체 이름
   * @return 다운로드한 파일 데이터
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   * @throws UncheckedIOException 응답 스트림을 읽거나 닫지 못한 경우
   */
  @Override
  public byte[] downloadFile(String fileName) {
    try (InputStream inputStream = download(fileName)) {
      return inputStream.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("failed to read file : " + fileName, e);
    }
  }

  /**
   * 파일명에 해당하는 OCI 객체의 응답 스트림을 반환한다.
   *
   * @param fileName 다운로드할 OCI 객체 이름
   * @return 호출자가 닫아야 하는 OCI 객체 응답 스트림
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   */
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
