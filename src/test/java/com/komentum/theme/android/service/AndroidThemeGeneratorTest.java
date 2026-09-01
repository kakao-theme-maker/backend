package com.komentum.theme.android.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.utils.FileManager;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport.ThemeComponentScenarioResult;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.theme.core.domain.ThemeComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("automated-test")
class AndroidThemeGeneratorTest {

  @Autowired
  private AndroidThemeGenerator androidThemeGenerator;
  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;
  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;
  @Autowired
  private FileManager fileManager;

  ThemeComponentScenarioResult themeComponentResult;

  @BeforeEach
  void setUp() {
    var userResult = userScenarioSupport.builder()
        .withUsers(1)
        .build();
    var designComponentResult = designComponentScenarioSupport.builder(userResult.users())
        .withCountPerUser(1)
        .build();
    themeComponentResult = themeComponentScenarioSupport.builder(userResult.users(),
            designComponentResult.designComponents())
        .withCountPerUser(1)
        .build();
  }

  @Test
  @Transactional
  void createAndSaveTheme_success() {
    // given
    ThemeComponent targetTheme = themeComponentResult.themeComponents().get(0);
    // when
    String themeUrl = androidThemeGenerator.createAndSaveTheme(targetTheme);
    // then
    String themeFileName = fileManager.convertUrlToFileName(themeUrl);
    assertThat(themeUrl).isNotBlank();
    assertThat(fileManager.downloadFile(themeFileName)).isNotEmpty();
  }
}
