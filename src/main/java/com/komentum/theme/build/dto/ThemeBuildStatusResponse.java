package com.komentum.theme.build.dto;

import com.komentum.theme.build.domain.ThemeBuildStatus;

public record ThemeBuildStatusResponse(
    ThemeBuildStatus status,
    String downloadUrl
) {

}
