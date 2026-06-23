package com.komentum.global.utils;

import java.awt.image.BufferedImage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageUtils {

  public static boolean compareImages(BufferedImage image1, BufferedImage image2) {
    int width = image1.getWidth();
    int height = image1.getHeight();
    if (image2.getWidth() != width || image2.getHeight() != height) {
      return false;
    }
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
          return false;
        }
      }
    }
    return true;
  }
}
