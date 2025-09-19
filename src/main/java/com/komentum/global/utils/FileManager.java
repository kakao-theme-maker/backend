package com.komentum.global.utils;

public interface FileManager {

  String uploadFile(byte[] fileBytes, String fileName);

  void deleteFile(String fileName);

  byte[] downloadFile(String fileName);
}
