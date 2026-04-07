package com.komentum.global.utils;

import java.util.List;

public interface FileManager {

  String resolveFilePath(String fileName);

  String convertUrlToFileName(String url);

  String uploadFile(byte[] fileBytes, String fileName);

  void deleteFile(String fileName);

  byte[] downloadFile(String fileName);

  List<String> listAllFileNames();
}