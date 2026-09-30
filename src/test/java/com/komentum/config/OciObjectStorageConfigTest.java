package com.komentum.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.objectstorage.ObjectStorage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OciObjectStorageConfigTest {

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(OciObjectStorageConfig.class);

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
            () -> new OciObjectStorageProperty(null, null, null, null, Duration.ofHours(48)))
        .withPropertyValues("file.storage=oci")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasRootCauseInstanceOf(IllegalArgumentException.class)
              .hasRootCauseMessage("oci.object-storage.namespace must not be blank");
        });
  }

  @Test
  @DisplayName("공개 이미지 버킷과 비공개 패키지 버킷이 같으면 context 시작에 실패한다")
  void objectStorage_sameBucket_contextFails() {
    contextRunner
        .withBean(OciObjectStorageProperty.class,
            () -> new OciObjectStorageProperty(
                "namespace", "shared-bucket", "shared-bucket", "endpoint",
                Duration.ofHours(48)))
        .withPropertyValues("file.storage=oci")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasRootCauseInstanceOf(IllegalArgumentException.class)
              .hasRootCauseMessage(
                  "oci.object-storage public and private bucket names must differ");
        });
  }

  @Test
  @DisplayName("parTtl이 0이면 context 시작에 실패한다")
  void objectStorage_invalidTtl_contextFails() {
    contextRunner
        .withBean(OciObjectStorageProperty.class,
            () -> new OciObjectStorageProperty(
                "namespace", "public-bucket", "private-bucket", "endpoint", Duration.ZERO))
        .withPropertyValues("file.storage=oci")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasRootCauseInstanceOf(IllegalArgumentException.class)
              .hasRootCauseMessage("oci.object-storage.par-ttl must be positive");
        });
  }

  @Test
  @DisplayName("parTtl이 음수이면 context 시작에 실패한다")
  void objectStorage_negativeTtl_contextFails() {
    contextRunner
        .withBean(OciObjectStorageProperty.class,
            () -> new OciObjectStorageProperty(
                "namespace", "public-bucket", "private-bucket", "endpoint",
                Duration.ofHours(-1)))
        .withPropertyValues("file.storage=oci")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasRootCauseInstanceOf(IllegalArgumentException.class)
              .hasRootCauseMessage("oci.object-storage.par-ttl must be positive");
        });
  }
}
