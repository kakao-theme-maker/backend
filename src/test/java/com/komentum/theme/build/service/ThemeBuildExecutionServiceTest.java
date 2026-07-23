package com.komentum.theme.build.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.service.ThemeBuildStateService.BuildContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThemeBuildExecutionServiceTest {

  private static final Long BUILD_ID = 1L;
  private static final Integer THEME_COMPONENT_ID = 101;

  @Mock
  private ThemeBuildStateService themeBuildStateService;

  @Mock
  private ThemePackageBuildHandler androidHandler;

  private ThemeBuildExecutionService themeBuildExecutionService;

  @BeforeEach
  void setUp() {
    given(androidHandler.platform()).willReturn(Platform.ANDROID);
    themeBuildExecutionService = new ThemeBuildExecutionService(
        Runnable::run,
        themeBuildStateService,
        List.of(androidHandler)
    );
  }

  @Test
  @DisplayName("플랫폼 handler가 반환한 URL로 성공 상태를 저장한다")
  void dispatch_success() {
    String packageUrl = "https://files.example.com/theme.apk";
    given(themeBuildStateService.loadRunningBuild(BUILD_ID))
        .willReturn(runningBuild(THEME_COMPONENT_ID, Platform.ANDROID));
    given(androidHandler.build(THEME_COMPONENT_ID)).willReturn(packageUrl);
    given(themeBuildStateService.markSuccess(
        eq(BUILD_ID),
        eq(packageUrl),
        any(LocalDateTime.class)
    )).willReturn(true);

    themeBuildExecutionService.dispatch(BUILD_ID);

    verify(androidHandler).build(THEME_COMPONENT_ID);
    verify(themeBuildStateService).markSuccess(
        eq(BUILD_ID),
        eq(packageUrl),
        any(LocalDateTime.class)
    );
  }

  @Test
  @DisplayName("이미 확정된 작업에는 성공 결과가 상태를 다시 변경하지 않는다")
  void dispatch_alreadyFinalized_doesNotMarkFailed() {
    String packageUrl = "https://files.example.com/theme.apk";
    given(themeBuildStateService.loadRunningBuild(BUILD_ID))
        .willReturn(runningBuild(THEME_COMPONENT_ID, Platform.ANDROID));
    given(androidHandler.build(THEME_COMPONENT_ID)).willReturn(packageUrl);
    given(themeBuildStateService.markSuccess(
        eq(BUILD_ID),
        eq(packageUrl),
        any(LocalDateTime.class)
    )).willReturn(false);

    themeBuildExecutionService.dispatch(BUILD_ID);

    verify(themeBuildStateService).markSuccess(
        eq(BUILD_ID),
        eq(packageUrl),
        any(LocalDateTime.class)
    );
    verify(themeBuildStateService, never()).markFailed(
        eq(BUILD_ID),
        any(LocalDateTime.class)
    );
  }

  @Test
  @DisplayName("플랫폼 handler 예외가 발생하면 작업을 실패 처리한다")
  void dispatch_handlerException_marksFailed() {
    given(themeBuildStateService.loadRunningBuild(BUILD_ID))
        .willReturn(runningBuild(THEME_COMPONENT_ID, Platform.ANDROID));
    given(androidHandler.build(THEME_COMPONENT_ID))
        .willThrow(new IllegalStateException("build failed"));

    themeBuildExecutionService.dispatch(BUILD_ID);

    verify(themeBuildStateService).markFailed(
        eq(BUILD_ID),
        any(LocalDateTime.class)
    );
    verify(themeBuildStateService, never()).markSuccess(
        eq(BUILD_ID),
        any(),
        any(LocalDateTime.class)
    );
  }

  @Test
  @DisplayName("플랫폼 handler가 빈 다운로드 URL을 반환하면 작업을 실패 처리한다")
  void dispatch_blankPackageUrl_marksFailed() {
    given(themeBuildStateService.loadRunningBuild(BUILD_ID))
        .willReturn(runningBuild(THEME_COMPONENT_ID, Platform.ANDROID));
    given(androidHandler.build(THEME_COMPONENT_ID)).willReturn(" ");

    themeBuildExecutionService.dispatch(BUILD_ID);

    verify(themeBuildStateService).markFailed(
        eq(BUILD_ID),
        any(LocalDateTime.class)
    );
    verify(themeBuildStateService, never()).markSuccess(
        eq(BUILD_ID),
        any(),
        any(LocalDateTime.class)
    );
  }

  @Test
  @DisplayName("플랫폼 handler가 없으면 작업을 실패 처리한다")
  void dispatch_missingHandler_marksFailed() {
    given(themeBuildStateService.loadRunningBuild(BUILD_ID))
        .willReturn(runningBuild(THEME_COMPONENT_ID, Platform.IOS));

    themeBuildExecutionService.dispatch(BUILD_ID);

    verify(themeBuildStateService).markFailed(
        eq(BUILD_ID),
        any(LocalDateTime.class)
    );
    verify(androidHandler, never()).build(THEME_COMPONENT_ID);
  }

  @Test
  @DisplayName("executor가 작업을 거절하면 작업을 실패 처리한다")
  void dispatch_executorRejected_marksFailed() {
    themeBuildExecutionService = new ThemeBuildExecutionService(
        command -> {
          throw new RejectedExecutionException("executor rejected build");
        },
        themeBuildStateService,
        List.of(androidHandler)
    );

    themeBuildExecutionService.dispatch(BUILD_ID);

    verify(themeBuildStateService).markFailed(
        eq(BUILD_ID),
        any(LocalDateTime.class)
    );
  }

  private java.util.Optional<BuildContext> runningBuild(
      Integer themeComponentId,
      Platform platform
  ) {
    return java.util.Optional.of(
        new BuildContext(themeComponentId, platform)
    );
  }
}
