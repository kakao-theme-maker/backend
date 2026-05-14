package com.komentum.global.enums;

import lombok.Getter;

@Getter
public enum FileExtension {
  PNG("png");

  private final String extension;

  FileExtension(String extension) {
    this.extension = extension;
  }
}
