package com.komentum.global.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CompressUtils {

  public static void unzip(InputStream zipFileInputStream, Path destination) throws IOException {
    destination = destination.toAbsolutePath().normalize();
    try (ZipInputStream zis = new ZipInputStream(zipFileInputStream)) {
      Files.createDirectories(destination);
      ZipEntry zipEntry;
      while ((zipEntry = zis.getNextEntry()) != null) {
        // 파일을 저장할 경로 단순화
        Path target = destination.resolve(zipEntry.getName()).normalize();
        // 파일을 저장할 경로의 유효성 검사
        if (!target.startsWith(destination)) {
          throw new IOException(
              "Invalid zip entry outside destination: " + zipEntry.getName()
          );
        }
        // 디렉토리면 생성, 파일이면 부모 디렉토리 생성 후 파일 복사
        if (zipEntry.isDirectory()) {
          Files.createDirectories(target);
        } else {
          Files.createDirectories(target.getParent());
          Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }
}
