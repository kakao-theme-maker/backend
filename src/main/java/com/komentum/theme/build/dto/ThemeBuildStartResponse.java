package com.komentum.theme.build.dto;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;

public record ThemeBuildStartResponse(
    Long buildId,
    Integer themeComponentId,
    Platform platform,
    ThemeBuildStatus status
) {

  public static ThemeBuildStartResponse from(ThemeBuildJob job) {
    return new ThemeBuildStartResponse(
        job.getBuildId(),
        job.getThemeComponent().getThemeComponentId(),
        job.getPlatform(),
        job.getStatus()
    );
  }
}
