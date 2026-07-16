package com.komentum.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.objectstorage.ObjectStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class OciObjectStorageConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(OciObjectStorageConfig.class);

  @Test
  @DisplayName("file.storage가 local이면 OCI ObjectStorage client를 생성하지 않는다")
  void objectStorage_fileStorageLocal_doesNotCreateBean() {
    contextRunner
        .withPropertyValues("file.storage=local")
        .run(context -> assertThat(context).doesNotHaveBean(ObjectStorage.class));
  }

  @Test
  @DisplayName("OCI Object Storage 환경변수 설정을 프로퍼티에 바인딩한다")
  void objectStorageProperty_validProperties_bindsValues() {
    new ApplicationContextRunner()
        .withUserConfiguration(PropertyBindingConfiguration.class)
        .withPropertyValues(
            "oci.object-storage.namespace=test-namespace",
            "oci.object-storage.bucket-name=MyObjectStorage",
            "oci.object-storage.endpoint=https://private.objectstorage.example.com",
            "oci.object-storage.par-base-url=https://objectstorage.example.com/p/test-token/o/"
        )
        .run(context -> {
          assertThat(context).hasNotFailed();
          OciObjectStorageProperty property = context.getBean(OciObjectStorageProperty.class);
          assertThat(property.getNamespace()).isEqualTo("test-namespace");
          assertThat(property.getBucketName()).isEqualTo("MyObjectStorage");
          assertThat(property.getEndpoint())
              .isEqualTo("https://private.objectstorage.example.com");
          assertThat(property.getParBaseUrl())
              .isEqualTo("https://objectstorage.example.com/p/test-token/o/");
        });
  }

  @Test
  @DisplayName("test profile에서는 OCI 선택 시에도 client를 생성하지 않는다")
  void objectStorage_testProfile_doesNotCreateBean() {
    contextRunner
        .withInitializer(context -> context.getEnvironment().setActiveProfiles("test"))
        .withPropertyValues("file.storage=oci")
        .run(context -> assertThat(context).doesNotHaveBean(ObjectStorage.class));
  }

  @Test
  @DisplayName("file.storage가 oci이면 필수 설정 누락 시 context 시작에 실패한다")
  void objectStorage_fileStorageOciWithMissingProperty_contextFails() {
    contextRunner
        .withBean(OciObjectStorageProperty.class,
            () -> new OciObjectStorageProperty(null, null, null, null))
        .withPropertyValues("file.storage=oci")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasRootCauseInstanceOf(IllegalArgumentException.class)
              .hasRootCauseMessage("oci.object-storage.namespace must not be blank");
        });
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(OciObjectStorageProperty.class)
  static class PropertyBindingConfiguration {
  }
}
