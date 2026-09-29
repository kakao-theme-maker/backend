package com.komentum.global.utils;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.model.PreauthenticatedRequest;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
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

  /**
   * OCI 공개 이미지 객체에 접근할 때 사용할 고정 URL 접두사를 생성한다.
   *
   * @return 정규화된 파일 URL 접두사
   */
  private String resolveFilePathPrefix() {
    return StringUtils.removeTrailingSlash(property.getEndpoint())
        + "/n/" + property.getNamespace()
        + "/b/" + property.getPublicImageBucketName()
        + "/o/";
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
   * 공개 이미지 객체 이름을 URL 인코딩된 고정 URL로 변환한다.
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
   * 비공개 테마 패키지 객체에 대한 읽기 전용 PAR URL을 생성한다.
   *
   * @param fileName OCI 객체 이름
   * @return 설정된 TTL 동안 유효한 다운로드 URL
   * @throws IllegalArgumentException 파일명이 null이거나 비어 있는 경우
   * @throws IllegalStateException OCI가 유효한 PAR을 반환하지 않은 경우
   */
  @Override
  public String createDownloadUrl(String fileName) {
    validateFileName(fileName);
    CreatePreauthenticatedRequestDetails details = CreatePreauthenticatedRequestDetails.builder()
        .name("theme-download-" + UUID.randomUUID())
        .objectName(fileName)
        .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
        .timeExpires(Date.from(Instant.now().plus(property.getParTtl())))
        .build();
    CreatePreauthenticatedRequestRequest request = CreatePreauthenticatedRequestRequest.builder()
        .namespaceName(property.getNamespace())
        .bucketName(property.getPrivateBucketName())
        .createPreauthenticatedRequestDetails(details)
        .build();
    CreatePreauthenticatedRequestResponse response =
        objectStorage.createPreauthenticatedRequest(request);
    PreauthenticatedRequest par = response == null ? null : response.getPreauthenticatedRequest();
    if (par == null || par.getAccessUri() == null || par.getAccessUri().isBlank()) {
      throw new IllegalStateException("preauthenticated request is null or empty");
    }
    return StringUtils.removeTrailingSlash(property.getEndpoint()) + "/"
        + StringUtils.trimSlash(par.getAccessUri());
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
   * 바이트 배열을 OCI 공개 이미지 버킷에 업로드하고 고정 URL을 반환한다.
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
   * 입력 스트림을 OCI 공개 이미지 버킷에 업로드하고 스트림을 닫은 뒤 고정 URL을 반환한다.
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
    uploadToBucket(is, contentLength, fileName, property.getPublicImageBucketName());
    return resolveFilePath(fileName);
  }

  @Override
  public String uploadAndGetFileName(byte[] fileBytes, String fileName, String fileExtension) {
    Objects.requireNonNull(fileBytes, "fileBytes is null");
    return uploadAndGetFileName(new ByteArrayInputStream(fileBytes), fileBytes.length,
        fileName, fileExtension);
  }

  @Override
  public String uploadAndGetFileName(InputStream is, long contentLength, String fileName,
      String fileExtension) {
    String resolvedFileName = resolveUploadFileName(fileName, fileExtension);
    uploadToBucket(is, contentLength, resolvedFileName, property.getPrivateBucketName());
    return resolvedFileName;
  }

  private String resolveUploadFileName(String fileName, String fileExtension) {
    if (fileExtension == null || fileExtension.isBlank()) {
      throw new IllegalArgumentException(
          "failed to resolve upload file name : fileExtension is empty");
    }
    String extension = fileExtension.startsWith(".") ? fileExtension.substring(1) : fileExtension;
    String suffix = "." + extension;
    if (fileName == null || fileName.isBlank()) {
      return UUID.randomUUID() + suffix;
    }
    return fileName.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT))
        ? fileName : fileName + suffix;
  }

  /**
   * 지정한 OCI 버킷에 객체를 업로드하고 입력 스트림을 닫는다.
   *
   * @param is 업로드할 파일 입력 스트림
   * @param contentLength 파일 크기
   * @param fileName OCI 객체 이름
   * @param bucketName 대상 버킷 이름
   */
  private void uploadToBucket(InputStream is, long contentLength, String fileName,
      String bucketName) {
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
          .bucketName(bucketName)
          .objectName(fileName)
          .contentLength(contentLength)
          .contentType(contentType)
          .putObjectBody(is)
          .build();
      objectStorage.putObject(request);
    } catch (IOException e) {
      throw new UncheckedIOException("failed to close upload stream : " + fileName, e);
    }
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
        .bucketName(property.getPublicImageBucketName())
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
        .bucketName(property.getPublicImageBucketName())
        .objectName(fileName)
        .build();
    return objectStorage.getObject(request).getInputStream();
  }
}
