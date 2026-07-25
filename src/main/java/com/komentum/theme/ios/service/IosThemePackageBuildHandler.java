package com.komentum.theme.ios.service;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.service.ThemePackageBuildHandler;
import com.komentum.theme.ios.editor.IosThemeMaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IosThemePackageBuildHandler implements ThemePackageBuildHandler {

  private final IosThemeMaker iosThemeMaker;

  @Override
  public Platform platform() {
    return Platform.IOS;
  }

  @Override
  public String build(Integer themeComponentId) {
    return iosThemeMaker.makeTheme(themeComponentId);
  }
}
