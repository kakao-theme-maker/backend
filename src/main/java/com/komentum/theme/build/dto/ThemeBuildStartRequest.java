package com.komentum.theme.build.dto;

import com.komentum.designcomponent.enums.Platform;
import jakarta.validation.constraints.NotNull;

public record ThemeBuildStartRequest(
    @NotNull Platform platform
) {
}
