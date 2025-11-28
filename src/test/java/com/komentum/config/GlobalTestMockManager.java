package com.komentum.config;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.S3FileManager;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class GlobalTestMockManager {

  @Bean
  @Primary
  public FileManager fileManager() {
    return Mockito.mock(S3FileManager.class);
  }
}
