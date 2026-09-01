package com.komentum.global.properties;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "file")
public class FileStorageProperty {

  @PostConstruct
  public void init() {
    log.info("FileStorage baseUrl={}", baseUrl);
  }

  private final String baseUrl;
  private final Storage storage;

  public static enum Storage {
    S3, LOCAL, OCI;
  }
}
