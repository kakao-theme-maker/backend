package com.komentum.global.properties;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oci.object-storage")
public class OciObjectStorageProperty {

  private final String namespace;
  private final String publicImageBucketName;
  private final String privateBucketName;
  private final String endpoint;
  private final Duration parTtl;
}
