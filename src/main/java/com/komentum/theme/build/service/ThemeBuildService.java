package com.komentum.theme.build.service;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.global.utils.FileManager;
import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import com.komentum.theme.build.dto.ThemeBuildStartResponse;
import com.komentum.theme.build.dto.ThemeBuildStatusResponse;
import com.komentum.theme.build.dto.ThemeDownloadResponse;
import com.komentum.theme.build.repository.ThemeBuildJobRepository;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ThemeBuildService {

  private final ThemeBuildJobRepository themeBuildJobRepository;
  private final ThemeComponentRepository themeComponentRepository;
  private final UserEntityFinder userEntityFinder;
  private final OwnerAdminPolicy ownerAdminPolicy;
  private final ThemeBuildExecutionService themeBuildExecutionService;
  private final FileManager fileManager;

  @Transactional
  public ThemeBuildStartResponse startBuild(Integer themeComponentId, Platform platform) {
    ThemeComponent themeComponent = themeComponentRepository.findByIdForUpdate(themeComponentId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Theme not found with id: " + themeComponentId));
    validateThemeAccess(themeComponent, "No permission to build theme package");
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

  /**
   * themeComponentId와 platform으로 가장 최근에 완료된 테마 빌드의 다운로드 URL을 조회한다. URL은 FileManager를 통해 조회한다.
   *
   * @param themeComponentId 다운로드할 테마 ID
   * @param platform         다운로드할 플랫폼
   * @return 다운로드 URL 정보
   * @throws ResourceNotFoundException 완료된 빌드가 없는 경우
   */
  @Transactional(readOnly = true)
  public ThemeDownloadResponse getDownloadUrl(Integer themeComponentId, Platform platform) {
    ThemeBuildJob job = themeBuildJobRepository
        .findFirstByThemeComponent_ThemeComponentIdAndPlatformAndStatusOrderByCreatedAtDesc(
            themeComponentId,
            platform,
            ThemeBuildStatus.SUCCESS
        )
        .orElseThrow(() -> new ResourceNotFoundException(
            "Completed theme build not found. themeComponentId: " + themeComponentId
                + ", platform: " + platform));
    return new ThemeDownloadResponse(job.getPackageUrl());
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
