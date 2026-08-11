package com.komentum.theme.build.repository;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeBuildJobRepository extends JpaRepository<ThemeBuildJob, Long> {

  Optional<ThemeBuildJob>
      findFirstByThemeComponent_ThemeComponentIdAndPlatformAndStatusOrderByCreatedAtDesc(
          Integer themeComponentId,
          Platform platform,
          ThemeBuildStatus status
      );

  Optional<ThemeBuildJob> findByBuildIdAndStatus(
      Long buildId,
      ThemeBuildStatus status
  );

  @Modifying(flushAutomatically = true)
  @Query("""
      update ThemeBuildJob job
      set job.status = com.komentum.theme.build.domain.ThemeBuildStatus.SUCCESS,
          job.packageUrl = :packageUrl,
          job.updatedAt = :updatedAt
      where job.buildId = :buildId
        and job.status = com.komentum.theme.build.domain.ThemeBuildStatus.RUNNING
      """)
  int markSuccessIfRunning(
      @Param("buildId") Long buildId,
      @Param("packageUrl") String packageUrl,
      @Param("updatedAt") LocalDateTime updatedAt
  );

  @Modifying(flushAutomatically = true)
  @Query("""
      update ThemeBuildJob job
      set job.status = com.komentum.theme.build.domain.ThemeBuildStatus.FAILED,
          job.updatedAt = :updatedAt
      where job.buildId = :buildId
        and job.status = com.komentum.theme.build.domain.ThemeBuildStatus.RUNNING
      """)
  int markFailedIfRunning(
      @Param("buildId") Long buildId,
      @Param("updatedAt") LocalDateTime updatedAt
  );
}
