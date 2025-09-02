package com.komentum.theme.theme.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.config.EnableTestProfile;
import com.komentum.test.ThemeDataGenerator;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableTestProfile
class ThemeRetrieveServiceTest {

  @Autowired
  private ThemeRetrieveService themeRetrieveService;

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  private final int initialThemeCount = 10;
  private final int initialStylePerTheme = 5;
  private final int initialImagePerTheme = 4;

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(initialThemeCount, initialStylePerTheme,
        initialImagePerTheme);
  }

  @AfterEach
  void tearDown() {
    themeDataGenerator.deleteTestData();
  }

  @Test
  @DisplayName("success test of retrieving all themes")
  void getAllThemes_success() {
    // when
    List<ThemeComponentDto> themeComponents = themeRetrieveService.getAllThemes();
    // then
    assertThat(themeComponents).hasSize(initialThemeCount).allSatisfy(themeComponentDto -> {
      assertThat(themeComponentDto.getStyles()).hasSize(initialStylePerTheme);
      assertThat(themeComponentDto.getImages()).hasSize(initialImagePerTheme);
    });
  }

  @Test
  @DisplayName("success test of retrieving theme by id")
  void getThemeById_success() {
    // given
    ThemeComponent toFind = themeDataGenerator.initialThemes.get(0);
    // when
    ThemeComponentDto founded = themeRetrieveService.getThemeById(toFind.getThemeComponentId());
    // then
    assertThat(founded.getThemeComponentId()).isEqualTo(toFind.getThemeComponentId());
    assertThat(founded.getStyles()).hasSize(initialStylePerTheme);
    assertThat(founded.getImages()).hasSize(initialImagePerTheme);
  }

  @Test
  @DisplayName("success test of retrieving theme by email")
  void getThemeByEmail_success() {
    // given
    String userEmail = themeDataGenerator.userEmail;
    long counts = themeDataGenerator.initialThemes.stream()
        .filter(theme -> theme.getUserEmail().equals(userEmail)).count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getThemesByUserEmail(userEmail);
    // then
    assertThat(founded).hasSize((int) counts).allSatisfy(
        themeComponentDto -> assertThat(themeComponentDto.getUserEmail()).isEqualTo(userEmail));
  }

  @Test
  @DisplayName("success test of retrieving public themes")
  void getPublicThemes_success() {
    // given
    long counts = themeDataGenerator.initialThemes.stream()
        .filter(ThemeComponent::getIsPublic).count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getPublicThemes();
    // then
    assertThat(founded).hasSize((int) counts)
        .allSatisfy(themeComponentDto -> assertThat(themeComponentDto.getIsPublic()).isTrue());
  }

  @Test
  @DisplayName("success test of retrieving completed themes")
  void getCompletedThemes_success() {
    // given
    long counts = themeDataGenerator.initialThemes.stream().filter(ThemeComponent::getIsDone)
        .count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getCompletedThemes();
    // then
    assertThat(founded).hasSize((int) counts)
        .allSatisfy(themeComponentDto -> assertThat(themeComponentDto.getIsDone()).isTrue());
  }

  @Test
  @DisplayName("success test of retrieving completed themes by user")
  void getCompletedThemesByUser_success() {
    // given
    String userEmail = themeDataGenerator.userEmail;
    long counts = themeDataGenerator.initialThemes.stream()
        .filter(ThemeComponent::getIsDone)
        .filter(theme -> theme.getUserEmail().equals(userEmail)).count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getCompletedThemesByUser(userEmail);
    // then
    assertThat(founded).hasSize((int) counts).allSatisfy(themeComponentDto -> {
      assertThat(themeComponentDto.getIsDone()).isTrue();
      assertThat(themeComponentDto.getUserEmail()).isEqualTo(userEmail);
    });
  }
}