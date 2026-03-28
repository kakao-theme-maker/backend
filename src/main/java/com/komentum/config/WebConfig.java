package com.komentum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  // 정적 파일이 저장되는 디렉토리
  public static final String UPLOAD_DIR = "/data/uploads";

  // 이미지 접근 시 사용하는 경로의 접두사
  public static final String UPLOAD_URL_PREFIX = "/data/uploads";

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler(UPLOAD_URL_PREFIX + "/**")
        .addResourceLocations("file:" + UPLOAD_DIR + "/");
  }
}
