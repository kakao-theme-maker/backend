package com.komentum.config;

import com.komentum.theme.utils.S3FileManager;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class GlobalTestMockManager {

  @Bean
  public S3FileManager s3FileManager() {
    return Mockito.mock(S3FileManager.class);
  }
}
