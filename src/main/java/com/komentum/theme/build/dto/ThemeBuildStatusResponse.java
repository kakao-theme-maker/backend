package com.komentum.theme.build.dto;

import com.komentum.theme.build.domain.ThemeBuildJob;
import com.komentum.theme.build.domain.ThemeBuildStatus;

public record ThemeBuildStatusResponse(
    ThemeBuildStatus status,
    String downloadUrl
) {

  public static ThemeBuildStatusResponse from(ThemeBuildJob job) {
    return new ThemeBuildStatusResponse(job.getStatus(), job.getPackageUrl());
  }
}
