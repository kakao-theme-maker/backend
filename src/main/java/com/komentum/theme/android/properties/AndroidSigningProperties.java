package com.komentum.theme.android.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "theme.android.signing")
public class AndroidSigningProperties {

  private final String keyAlias;
  private final String keyPassword;
  private final String keystorePassword;
  private final String keystoreHostPath;
}
