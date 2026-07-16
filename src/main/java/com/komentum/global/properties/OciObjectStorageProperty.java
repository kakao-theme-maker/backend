package com.komentum.global.properties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oci.object-storage")
public class OciObjectStorageProperty {

  private final String namespace;
  private final String bucketName;
  private final String endpoint;
  private final String parBaseUrl;
}
