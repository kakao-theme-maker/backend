package com.komentum.theme.build.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThemeBuildConfig {

  public static final String THEME_BUILD_EXECUTOR = "themeBuildExecutor";

  @Bean(name = THEME_BUILD_EXECUTOR)
  public Executor themeBuildExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(20);
    executor.setThreadNamePrefix("theme-build-");
    return executor;
  }
}
