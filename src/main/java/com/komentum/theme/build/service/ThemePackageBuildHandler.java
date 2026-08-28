package com.komentum.theme.build.service;

import com.komentum.designcomponent.enums.Platform;

public interface ThemePackageBuildHandler {

  Platform platform();

  String build(Integer themeComponentId);
}
