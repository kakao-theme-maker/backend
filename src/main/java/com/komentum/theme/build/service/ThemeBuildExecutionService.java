package com.komentum.theme.build.service;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.config.ThemeBuildConfig;
import com.komentum.theme.build.service.ThemeBuildStateService.BuildContext;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThemeBuildExecutionService {

  private final Executor themeBuildExecutor;
  private final ThemeBuildStateService themeBuildStateService;
  private final Map<Platform, ThemePackageBuildHandler> handlers;

  public ThemeBuildExecutionService(
      @Qualifier(ThemeBuildConfig.THEME_BUILD_EXECUTOR) Executor themeBuildExecutor,
      ThemeBuildStateService themeBuildStateService,
      List<ThemePackageBuildHandler> buildHandlers
  ) {
    this.themeBuildExecutor = themeBuildExecutor;
    this.themeBuildStateService = themeBuildStateService;

    Map<Platform, ThemePackageBuildHandler> handlerMap = new EnumMap<>(Platform.class);
    for (ThemePackageBuildHandler handler : buildHandlers) {
      ThemePackageBuildHandler duplicate = handlerMap.put(handler.platform(), handler);
      if (duplicate != null) {
        throw new IllegalStateException(
            "Duplicate theme package build handler for platform: " + handler.platform());
      }
    }
    this.handlers = Map.copyOf(handlerMap);
  }

  public void dispatch(Long buildId) {
    try {
      themeBuildExecutor.execute(() -> runBuild(buildId));
    } catch (RuntimeException e) {
      log.error("[ThemeBuildExecutionService] Failed to dispatch build. buildId={}", buildId, e);
      try {
        themeBuildStateService.markFailed(buildId, LocalDateTime.now());
      } catch (RuntimeException stateException) {
        log.error("[ThemeBuildExecutionService] Failed to persist dispatch rejection. buildId={}",
            buildId, stateException);
      }
    }
  }

  private void runBuild(Long buildId) {
    BuildContext context;
    try {
      context = themeBuildStateService.loadRunningBuild(buildId).orElse(null);
    } catch (RuntimeException e) {
      log.error("[ThemeBuildExecutionService] Failed to load build. buildId={}", buildId, e);
      return;
    }
    if (context == null) {
      return;
    }

    ThemePackageBuildHandler handler = handlers.get(context.platform());
    if (handler == null) {
      log.warn("[ThemeBuildExecutionService] No build handler. buildId={}, platform={}",
          buildId, context.platform());
      themeBuildStateService.markFailed(buildId, LocalDateTime.now());
      return;
    }

    try {
      String packageUrl = handler.build(context.themeComponentId());
      if (packageUrl == null || packageUrl.isBlank()) {
        log.warn("[ThemeBuildExecutionService] Build returned an empty URL. buildId={}", buildId);
        themeBuildStateService.markFailed(buildId, LocalDateTime.now());
        return;
      }

      if (!themeBuildStateService.markSuccess(buildId, packageUrl, LocalDateTime.now())) {
        log.warn("[ThemeBuildExecutionService] Build status was already finalized. buildId={}",
            buildId);
      }
    } catch (Exception e) {
      log.error("[ThemeBuildExecutionService] Failed to build theme. buildId={}", buildId, e);
      try {
        themeBuildStateService.markFailed(buildId, LocalDateTime.now());
      } catch (RuntimeException stateException) {
        log.error("[ThemeBuildExecutionService] Failed to persist build failure. buildId={}",
            buildId, stateException);
      }
    }
  }
}
