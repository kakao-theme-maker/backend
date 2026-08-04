package com.komentum.theme.android.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "theme.android.docker")
public class AndroidDockerImageProperties {

  private final String image;
  private final String tag;
}
