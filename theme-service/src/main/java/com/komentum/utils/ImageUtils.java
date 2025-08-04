package com.komentum.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ImageUtils {

  public static byte[] loadImageBytes(String imageUrl) {
    try {
      Path imagePath = Paths.get(imageUrl);
      return Files.readAllBytes(imagePath);
    } catch (IOException e) {
      log.error(e.getMessage());
      return null;
    }
  }
}
