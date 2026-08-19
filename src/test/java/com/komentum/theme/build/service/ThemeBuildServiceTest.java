package com.komentum.theme.build.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.UserRole;
import com.komentum.test.fixture.theme.ThemeBuildFixture;
import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import com.komentum.theme.build.dto.ThemeBuildStartResponse;
import com.komentum.theme.build.repository.ThemeBuildJobRepository;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.service.ThemeManageService;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class ThemeBuildServiceTest {

  @Autowired
  private ThemeBuildService themeBuildService;
  @Autowired
  private ThemeManageService themeManageService;
  @Autowired
  private ThemeBuildJobRepository themeBuildJobRepository;
  @Autowired
  private ThemeComponentRepository themeComponentRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PlatformTransactionManager transactionManager;
  @Autowired
  private EntityManager entityManager;

  @MockitoBean
  private ThemeBuildExecutionService themeBuildExecutionService;

  private User owner;
  private ThemeComponent theme;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(
        ThemeBuildFixture.user("theme-build-service-owner@test.com", UserRole.USER));
    theme = themeComponentRepository.save(ThemeBuildFixture.theme(owner.getUserEmail()));
    authenticate(owner);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    themeBuildJobRepository.deleteAll();
    themeComponentRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("job commit이 끝난 뒤 제작 실행을 dispatch한다")
  void startBuild_dispatchesAfterCommit() {
    AtomicBoolean jobVisibleFromNewTransaction = new AtomicBoolean(false);
    AtomicReference<ThemeBuildStartResponse> resultReference = new AtomicReference<>();
    doAnswer(invocation -> {
      Long buildId = invocation.getArgument(0);
      TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
      transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      Boolean visible = transactionTemplate.execute(
          status -> themeBuildJobRepository.existsById(buildId));
      jobVisibleFromNewTransaction.set(Boolean.TRUE.equals(visible));
      return null;
    }).when(themeBuildExecutionService).dispatch(anyLong());

    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.executeWithoutResult(status -> {
      resultReference.set(themeBuildService.startBuild(
          theme.getThemeComponentId(),
          Platform.ANDROID
      ));
      verify(themeBuildExecutionService, never()).dispatch(anyLong());
    });

    ThemeBuildStartResponse result = resultReference.get();
    assertThat(jobVisibleFromNewTransaction).isTrue();
    assertThat(result.status()).isEqualTo(ThemeBuildStatus.RUNNING);
    verify(themeBuildExecutionService).dispatch(result.buildId());
  }

  @Test
  @DisplayName("job 생성 트랜잭션이 rollback되면 제작 실행을 dispatch하지 않는다")
  void startBuild_doesNotDispatchAfterRollback() {
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

    transactionTemplate.executeWithoutResult(status -> {
      themeBuildService.startBuild(theme.getThemeComponentId(), Platform.ANDROID);
      verify(themeBuildExecutionService, never()).dispatch(anyLong());
      status.setRollbackOnly();
    });

    verify(themeBuildExecutionService, never()).dispatch(anyLong());
    assertThat(themeBuildJobRepository.count()).isZero();
  }

  @Test
  @DisplayName("동일 테마와 플랫폼의 RUNNING job은 재사용한다")
  void startBuild_reusesRunningJob() {
    ThemeBuildStartResponse first = themeBuildService.startBuild(
        theme.getThemeComponentId(), Platform.ANDROID);
    ThemeBuildStartResponse second = themeBuildService.startBuild(
        theme.getThemeComponentId(), Platform.ANDROID);

    assertThat(second.buildId()).isEqualTo(first.buildId());
    assertThat(themeBuildJobRepository.count()).isOne();
    verify(themeBuildExecutionService).dispatch(first.buildId());
  }

  @Test
  @DisplayName("같은 테마의 Android와 iOS 제작 작업은 독립적으로 생성한다")
  void startBuild_createsIndependentJobsByPlatform() {
    ThemeBuildStartResponse android = themeBuildService.startBuild(
        theme.getThemeComponentId(), Platform.ANDROID);
    ThemeBuildStartResponse ios = themeBuildService.startBuild(
        theme.getThemeComponentId(), Platform.IOS);

    assertThat(ios.buildId()).isNotEqualTo(android.buildId());
    assertThat(themeBuildJobRepository.findAll())
        .extracting(ThemeBuildJob::getPlatform)
        .containsExactlyInAnyOrder(Platform.ANDROID, Platform.IOS);
    verify(themeBuildExecutionService).dispatch(android.buildId());
    verify(themeBuildExecutionService).dispatch(ios.buildId());
  }

  @Test
  @DisplayName("동시 제작 요청도 하나의 RUNNING job만 생성한다")
  void startBuild_concurrentRequestsReuseOneJob() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      Future<Long> first = executor.submit(
          () -> startBuildAfterSignal(ready, start));
      Future<Long> second = executor.submit(
          () -> startBuildAfterSignal(ready, start));

      assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
      start.countDown();

      Long firstBuildId = first.get(10, TimeUnit.SECONDS);
      Long secondBuildId = second.get(10, TimeUnit.SECONDS);
      assertThat(secondBuildId).isEqualTo(firstBuildId);
      assertThat(themeBuildJobRepository.count()).isOne();
      verify(themeBuildExecutionService).dispatch(firstBuildId);
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  @Transactional
  @DisplayName("테마를 삭제하면 DB cascade로 관련 build job도 삭제된다")
  void deleteTheme_cascadesBuildJobs() {
    ThemeBuildJob build = themeBuildJobRepository.saveAndFlush(
        ThemeBuildJob.createRunning(theme, Platform.ANDROID));
    Integer themeComponentId = theme.getThemeComponentId();
    Long buildId = build.getBuildId();
    entityManager.clear();

    themeManageService.deleteTheme(themeComponentId);
    entityManager.flush();
    entityManager.clear();

    assertThat(themeComponentRepository.existsById(themeComponentId)).isFalse();
    assertThat(themeBuildJobRepository.existsById(buildId)).isFalse();
  }

  private Long startBuildAfterSignal(CountDownLatch ready, CountDownLatch start) throws Exception {
    authenticate(owner);
    ready.countDown();
    try {
      if (!start.await(5, TimeUnit.SECONDS)) {
        throw new IllegalStateException("concurrent build start signal timed out");
      }
      return themeBuildService.startBuild(theme.getThemeComponentId(), Platform.ANDROID).buildId();
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private void authenticate(User user) {
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .userEmail(user.getUserEmail())
        .publicUserId(user.getPublicUserId())
        .userRole(user.getRole())
        .build();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        ));
  }
}
