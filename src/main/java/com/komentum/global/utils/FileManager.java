package com.komentum.global.utils;

import java.io.InputStream;
import java.util.UUID;

public interface FileManager {

  /**
   * 파일명을 파일 URL로 변환한다
   * */
  String resolveFilePath(String fileName);

  /**
   * 파일 URL을 파일명으로 변환한다
   * */
  String convertUrlToFileName(String url);

  /**
   * 파일을 업로드하고, URL을 반환한다
   * */
  String uploadFile(byte[] fileBytes, String fileName);

  String uploadFile(InputStream is, long contentLength, String fileName);

  /**
   * 파일을 업로드하고, 저장소에 저장된 파일명(항상 확장자 포함)을 반환한다.
   * fileName이 null이거나 공백이면 UUID + fileExtension 형식(예: 550e8400-....apk)으로 생성한다.
   * fileName이 지정되었지만 fileExtension으로 끝나지 않으면 확장자를 덧붙인다.
   *
   * @param fileName      저장할 파일명 (생략 가능)
   * @param fileExtension 파일 확장자 ("apk" 또는 ".apk"), 필수
   * @throws IllegalArgumentException fileExtension이 비어 있는 경우
   * */
  default String uploadAndGetFileName(byte[] fileBytes, String fileName, String fileExtension) {
    String resolvedFileName = resolveUploadFileName(fileName, fileExtension);
    uploadFile(fileBytes, resolvedFileName);
    return resolvedFileName;
  }

  /**
   * 파일을 업로드하고, 저장소에 저장된 파일명(확장자 포함)을 반환한다.
   * 파일명 생성 규칙은 {@link #uploadAndGetFileName(byte[], String, String)}과 동일하다.
   * */
  default String uploadAndGetFileName(InputStream is, long contentLength, String fileName,
      String fileExtension) {
    String resolvedFileName = resolveUploadFileName(fileName, fileExtension);
    uploadFile(is, contentLength, resolvedFileName);
    return resolvedFileName;
  }

  /**
   * fileName과 fileExtension을 기반으로 파일 확장자가 포함된 문자열을 반환한다
   * fileName이 NULL이라면 UUID를 fileName으로 사용한다
   * */
  private static String resolveUploadFileName(String fileName, String fileExtension) {
    if (fileExtension == null || fileExtension.isBlank()) {
      throw new IllegalArgumentException(
          "failed to resolve upload file name : fileExtension is empty");
    }
    String extension = fileExtension.startsWith(".") ? fileExtension.substring(1) : fileExtension;
    String suffix = "." + extension;
    if (fileName == null || fileName.isBlank()) {
      return UUID.randomUUID() + suffix;
    }
    return fileName.toLowerCase().endsWith(suffix.toLowerCase()) ? fileName : fileName + suffix;
  }

  /**
   * 파일명을 기반으로 특정 파일을 삭제한다
   * */
  void deleteFile(String fileName);

  /**
   * 파일을 byte[] 형태로 다운로드한다
   * */
  byte[] downloadFile(String fileName);

  /**
   * 파일을 InputStream 형태로 다운로드한다
   * */
  InputStream download(String fileName);
}
