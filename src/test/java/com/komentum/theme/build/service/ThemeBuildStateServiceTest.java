package com.komentum.theme.build.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.fixture.theme.ThemeBuildFixture;
import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import com.komentum.theme.build.repository.ThemeBuildJobRepository;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableTestProfile
class ThemeBuildStateServiceTest {

  @Autowired
  private ThemeBuildStateService themeBuildStateService;
  @Autowired
  private ThemeBuildJobRepository themeBuildJobRepository;
  @Autowired
  private ThemeComponentRepository themeComponentRepository;

  private ThemeComponent theme;

  @BeforeEach
  void setUp() {
    theme = themeComponentRepository.save(
        ThemeBuildFixture.theme("theme-build-state@test.com"));
  }

  @AfterEach
  void tearDown() {
    themeBuildJobRepository.deleteAll();
    themeComponentRepository.deleteAll();
  }

  @Test
  @DisplayName("다운로드 URL을 받은 RUNNING job을 SUCCESS로 변경한다")
  void markSuccess_success() {
    ThemeBuildJob job = saveRunningJob();
    LocalDateTime updatedAt = LocalDateTime.now();
    String packageUrl = "https://files.example.com/theme.apk";

    boolean updated = themeBuildStateService.markSuccess(
        job.getBuildId(), packageUrl, updatedAt);

    ThemeBuildJob result = themeBuildJobRepository.findById(job.getBuildId()).orElseThrow();
    assertThat(updated).isTrue();
    assertThat(result.getStatus()).isEqualTo(ThemeBuildStatus.SUCCESS);
    assertThat(result.getPackageUrl()).isEqualTo(packageUrl);
    assertThat(result.getUpdatedAt()).isCloseTo(updatedAt, byLessThan(2, ChronoUnit.SECONDS));
  }

  @Test
  @DisplayName("RUNNING job을 FAILED로 변경한다")
  void markFailed_success() {
    ThemeBuildJob job = saveRunningJob();
    LocalDateTime updatedAt = LocalDateTime.now();

    themeBuildStateService.markFailed(job.getBuildId(), updatedAt);

    ThemeBuildJob result = themeBuildJobRepository.findById(job.getBuildId()).orElseThrow();
    assertThat(result.getStatus()).isEqualTo(ThemeBuildStatus.FAILED);
    assertThat(result.getPackageUrl()).isNull();
    assertThat(result.getUpdatedAt()).isCloseTo(updatedAt, byLessThan(2, ChronoUnit.SECONDS));
  }

  @Test
  @DisplayName("이미 확정된 FAILED 상태는 늦은 SUCCESS가 덮어쓰지 못한다")
  void terminalState_isNotOverwritten() {
    ThemeBuildJob job = saveRunningJob();
    LocalDateTime failedAt = LocalDateTime.now();
    themeBuildStateService.markFailed(job.getBuildId(), failedAt);

    boolean updated = themeBuildStateService.markSuccess(
        job.getBuildId(),
        "https://files.example.com/late-theme.apk",
        LocalDateTime.now()
    );

    ThemeBuildJob result = themeBuildJobRepository.findById(job.getBuildId()).orElseThrow();
    assertThat(updated).isFalse();
    assertThat(result.getStatus()).isEqualTo(ThemeBuildStatus.FAILED);
    assertThat(result.getPackageUrl()).isNull();
    assertThat(result.getUpdatedAt()).isCloseTo(failedAt, byLessThan(2, ChronoUnit.SECONDS));
  }

  private ThemeBuildJob saveRunningJob() {
    return themeBuildJobRepository.saveAndFlush(
        ThemeBuildJob.createRunning(theme, Platform.ANDROID));
  }
}
