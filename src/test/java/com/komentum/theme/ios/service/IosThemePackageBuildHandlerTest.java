package com.komentum.theme.ios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.ios.editor.IosThemeMaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IosThemePackageBuildHandlerTest {

  @Mock
  private IosThemeMaker iosThemeMaker;

  private IosThemePackageBuildHandler handler;

  @BeforeEach
  void setUp() {
    handler = new IosThemePackageBuildHandler(iosThemeMaker);
  }

  @Test
  @DisplayName("iOS handler는 기존 테마 제작기에 위임하고 URL을 반환한다")
  void build_delegatesToExistingMaker() {
    Integer themeComponentId = 1;
    String packageUrl = "https://files.example.com/theme.ktheme";
    when(iosThemeMaker.makeTheme(themeComponentId)).thenReturn(packageUrl);

    String result = handler.build(themeComponentId);

    assertThat(handler.platform()).isEqualTo(Platform.IOS);
    assertThat(result).isEqualTo(packageUrl);
    verify(iosThemeMaker).makeTheme(themeComponentId);
  }
}
