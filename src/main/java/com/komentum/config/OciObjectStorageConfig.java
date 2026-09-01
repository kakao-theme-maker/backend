package com.komentum.config;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
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
   * @throws IllegalArgumentException namespace, bucket name 또는 endpoint가 비어 있는 경우
   */
  @Bean(destroyMethod = "close")
  public ObjectStorage objectStorage() {
    Assert.hasText(property.getNamespace(), "oci.object-storage.namespace must not be blank");
    Assert.hasText(property.getBucketName(), "oci.object-storage.bucket-name must not be blank");
    Assert.hasText(property.getEndpoint(), "oci.object-storage.endpoint must not be blank");
    InstancePrincipalsAuthenticationDetailsProvider provider =
        InstancePrincipalsAuthenticationDetailsProvider.builder().build();
    return ObjectStorageClient.builder()
        .endpoint(property.getEndpoint())
        .build(provider);
  }
}
