package com.komentum.theme.android.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "theme.android.signing")
public class AndroidSigningProperties {

  @NotBlank
  private final String keyAlias;
  @NotBlank
  private final String keyPassword;
  @NotBlank
  private final String keystorePassword;
  @NotBlank
  private final String keystoreHostPath;
}
