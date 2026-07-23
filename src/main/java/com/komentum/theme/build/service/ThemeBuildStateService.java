package com.komentum.theme.build.service;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import com.komentum.theme.build.repository.ThemeBuildJobRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeBuildStateService {

  private final ThemeBuildJobRepository themeBuildJobRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean markSuccess(Long buildId, String packageUrl, LocalDateTime updatedAt) {
    return themeBuildJobRepository.markSuccessIfRunning(
        buildId,
        packageUrl,
        updatedAt,
        ThemeBuildStatus.RUNNING,
        ThemeBuildStatus.SUCCESS
    ) == 1;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean markFailed(Long buildId, LocalDateTime updatedAt) {
    return themeBuildJobRepository.markFailedIfRunning(
        buildId,
        updatedAt,
        ThemeBuildStatus.RUNNING,
        ThemeBuildStatus.FAILED
    ) == 1;
  }

  @Transactional(readOnly = true)
  public Optional<BuildContext> loadRunningBuild(Long buildId) {
    return themeBuildJobRepository.findByBuildIdAndStatus(buildId, ThemeBuildStatus.RUNNING)
        .map(job -> new BuildContext(
            job.getThemeComponent().getThemeComponentId(),
            job.getPlatform()
        ));
  }

  public record BuildContext(
      Integer themeComponentId,
      Platform platform
  ) {
  }
}
