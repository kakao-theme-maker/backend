package com.komentum.theme.android.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.service.ThemeRetrieveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AndroidThemePackageBuildHandlerTest {

  @Mock
  private ThemeRetrieveService themeRetrieveService;
  @Mock
  private AndroidThemeGenerator androidThemeGenerator;

  private AndroidThemePackageBuildHandler handler;

  @BeforeEach
  void setUp() {
    handler = new AndroidThemePackageBuildHandler(themeRetrieveService, androidThemeGenerator);
  }

  @Test
  @DisplayName("Android handler는 기존 APK 제작기에 위임하고 URL을 반환한다")
  void build_delegatesToExistingGenerator() {
    Integer themeComponentId = 1;
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(themeComponentId)
        .build();
    String packageUrl = "https://files.example.com/theme.apk";
    when(themeRetrieveService.getThemeEntityById(themeComponentId)).thenReturn(themeComponent);
    when(androidThemeGenerator.createAndSaveTheme(themeComponent)).thenReturn(packageUrl);

    String result = handler.build(themeComponentId);

    assertThat(handler.platform()).isEqualTo(Platform.ANDROID);
    assertThat(result).isEqualTo(packageUrl);
    verify(themeRetrieveService).getThemeEntityById(themeComponentId);
    verify(androidThemeGenerator).createAndSaveTheme(themeComponent);
  }
}
