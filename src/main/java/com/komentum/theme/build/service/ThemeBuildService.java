package com.komentum.theme.build.service;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import com.komentum.theme.build.dto.ThemeBuildStartResponse;
import com.komentum.theme.build.dto.ThemeBuildStatusResponse;
import com.komentum.theme.build.repository.ThemeBuildJobRepository;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ThemeBuildService {

  private final ThemeBuildJobRepository themeBuildJobRepository;
  private final ThemeComponentRepository themeComponentRepository;
  private final UserEntityFinder userEntityFinder;
  private final OwnerAdminPolicy ownerAdminPolicy;
  private final ThemeBuildExecutionService themeBuildExecutionService;

  @Transactional
  public ThemeBuildStartResponse startBuild(Integer themeComponentId, Platform platform) {
    ThemeComponent themeComponent = themeComponentRepository.findByIdForUpdate(themeComponentId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Theme not found with id: " + themeComponentId));
    validateThemeAccess(themeComponent, "No permission to build theme package");
    if (!themeBuildExecutionService.supports(platform)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Unsupported theme build platform: " + platform
      );
    }

    ThemeBuildJob runningJob = themeBuildJobRepository
        .findFirstByThemeComponent_ThemeComponentIdAndPlatformAndStatusOrderByCreatedAtDesc(
            themeComponentId,
            platform,
            ThemeBuildStatus.RUNNING
        )
        .orElse(null);
    if (runningJob != null) {
      return ThemeBuildStartResponse.from(runningJob);
    }

    ThemeBuildJob saved = themeBuildJobRepository.saveAndFlush(
        ThemeBuildJob.createRunning(themeComponent, platform)
    );
    registerBuildDispatch(saved.getBuildId());
    return ThemeBuildStartResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public ThemeBuildStatusResponse findBuild(Long buildId) {
    ThemeBuildJob job = themeBuildJobRepository.findById(buildId)
        .orElseThrow(() -> new ResourceNotFoundException("Theme build not found"));
    validateThemeAccess(job.getThemeComponent(), "No permission to access theme build");
    return ThemeBuildStatusResponse.from(job);
  }

  private void validateThemeAccess(ThemeComponent themeComponent, String errorMessage) {
    User owner = userEntityFinder.findUserEntityByEmail(themeComponent.getUserEmail());
    if (!ownerAdminPolicy.validate(owner)) {
      throw new AccessDeniedException(errorMessage);
    }
  }

  private void registerBuildDispatch(Long buildId) {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        themeBuildExecutionService.dispatch(buildId);
      }
    });
  }
}
