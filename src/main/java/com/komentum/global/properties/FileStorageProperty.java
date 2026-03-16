package com.komentum.global.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "file")
public class FileStorageProperty {

  private final String baseUrl;
  private final Storage storage;

  public static enum Storage {
    S3, LOCAL;
  }
}
