package com.komentum.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.test.config.EnableTestProfile;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 빈 MySQL에 V1(구 스키마 baseline)부터 최신 마이그레이션까지 적용한 결과가 현재 엔티티와 일치하는지 검증한다.
 * Hibernate ddl-auto=validate로 컨텍스트를 띄우므로, 엔티티 변경에 맞는 마이그레이션이 빠지면 컨텍스트 로딩이 실패한다.
 * Docker가 없는 환경에서는 실행되지 않는다.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@EnableTestProfile
class FlywayMigrationSchemaTest {

  // 운영 DB와 같은 버전으로 고정한다
  @Container
  static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.46");

  @DynamicPropertySource
  static void mysqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
    registry.add("spring.datasource.username", MYSQL::getUsername);
    registry.add("spring.datasource.password", MYSQL::getPassword);
    registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  @Autowired
  private Flyway flyway;

  @Test
  @DisplayName("모든 마이그레이션을 적용한 MySQL 스키마가 현재 엔티티 매핑과 일치한다")
  void migratedSchemaMatchesEntityMapping() {
    // 컨텍스트 로딩 시 flyway migrate → hibernate validate가 수행되므로, 여기서는 적용 상태를 확인한다
    MigrationInfoService info = flyway.info();
    MigrationInfo[] resolved = info.all();

    assertThat(info.pending()).isEmpty();
    assertThat(info.current()).isNotNull();
    assertThat(info.current().getVersion())
        .isEqualTo(resolved[resolved.length - 1].getVersion());
    assertThat(info.applied())
        .allSatisfy(migration -> assertThat(migration.getState().isFailed()).isFalse());
  }
}
