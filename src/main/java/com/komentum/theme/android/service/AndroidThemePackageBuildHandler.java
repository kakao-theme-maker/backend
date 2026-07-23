package com.komentum.theme.android.service;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.service.ThemePackageBuildHandler;
import com.komentum.theme.core.service.ThemeRetrieveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AndroidThemePackageBuildHandler implements ThemePackageBuildHandler {

  private final ThemeRetrieveService themeRetrieveService;
  private final AndroidThemeGenerator androidThemeGenerator;

  @Override
  public Platform platform() {
    return Platform.ANDROID;
  }

  @Override
  public String build(Integer themeComponentId) {
    return androidThemeGenerator.createAndSaveTheme(
        themeRetrieveService.getThemeEntityById(themeComponentId)
    );
  }
}
