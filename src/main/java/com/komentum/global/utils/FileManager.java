package com.komentum.global.utils;

import java.io.InputStream;

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
