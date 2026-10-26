package com.komentum.theme.android.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@ConfigurationProperties(prefix = "theme.android.docker")
public class AndroidDockerImageProperties {

  @NotBlank
  private final String image;
  @NotBlank
  private final String tag;
}
