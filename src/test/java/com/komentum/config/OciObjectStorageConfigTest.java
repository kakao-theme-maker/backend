package com.komentum.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.objectstorage.ObjectStorage;
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
            () -> new OciObjectStorageProperty(null, null, null))
        .withPropertyValues("file.storage=oci")
        .run(context -> {
          assertThat(context).hasFailed();
          assertThat(context.getStartupFailure())
              .hasRootCauseInstanceOf(IllegalArgumentException.class)
              .hasRootCauseMessage("oci.object-storage.namespace must not be blank");
        });
  }

}
