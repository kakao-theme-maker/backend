package com.komentum.global.utils;

import java.io.IOException;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

@Slf4j
public class CustomFileUtils {

  /**
   * 지정된 디렉토리를 삭제한다.
   * 삭제에 실패하더라도 예외를 전파하지 않는다.
   */
  public static void deleteDirectorySilently(Path path) {
    try {
      FileUtils.deleteDirectory(path.toFile());
    } catch (IOException e) {
      log.warn("Failed to delete file : {}", path.getFileName());
    }
  }
}
