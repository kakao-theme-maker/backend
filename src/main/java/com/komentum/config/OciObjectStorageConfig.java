package com.komentum.config;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage", havingValue = "oci")
public class OciObjectStorageConfig {

  private final OciObjectStorageProperty property;

  /**
   * 필수 OCI 설정을 검증하고 Instance Principal 인증 기반 Object Storage 클라이언트를 생성한다.
   *
   * @return OCI Object Storage 클라이언트
   * @throws IllegalArgumentException namespace, bucket name, endpoint 또는 PAR TTL이 유효하지 않은 경우
   */
  @Bean(destroyMethod = "close")
  public ObjectStorage objectStorage() {
    Assert.hasText(property.getNamespace(), "oci.object-storage.namespace must not be blank");
    Assert.hasText(property.getPublicImageBucketName(),
        "oci.object-storage.public-image-bucket-name must not be blank");
    Assert.hasText(property.getPrivateBucketName(),
        "oci.object-storage.private-bucket-name must not be blank");
    Assert.isTrue(!property.getPublicImageBucketName().equals(property.getPrivateBucketName()),
        "oci.object-storage public and private bucket names must differ");
    Assert.hasText(property.getEndpoint(), "oci.object-storage.endpoint must not be blank");
    Assert.notNull(property.getParTtl(), "oci.object-storage.par-ttl must not be null");
    Assert.isTrue(property.getParTtl().compareTo(Duration.ZERO) > 0,
        "oci.object-storage.par-ttl must be positive");
    InstancePrincipalsAuthenticationDetailsProvider provider =
        InstancePrincipalsAuthenticationDetailsProvider.builder().build();
    return ObjectStorageClient.builder()
        .endpoint(property.getEndpoint())
        .build(provider);
  }
}
