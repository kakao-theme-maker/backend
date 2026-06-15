package com.komentum.global.enums;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum FileExtension {
  PNG("png");

  private final String extension;

  FileExtension(String extension) {
    this.extension = extension;
  }

  public static FileExtension from(String extension) {
    return Arrays.stream(values())
        .filter(fileExtension -> fileExtension.extension.equals(extension))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unsupported file extension: " + extension));
  }
}
