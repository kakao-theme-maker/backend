package com.komentum.theme.theme.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.config.EnableTestProfile;
import com.komentum.test.ThemeDataGenerator;
import com.komentum.theme.component.dto.CreateThemeRequest;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemeImageRequest;
import com.komentum.theme.theme.dto.ThemeStyleRequest;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@EnableTestProfile
class ThemeManageServiceTest {

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  @Autowired
  private ThemeManageService themeManageService;

  @Autowired
  private ThemeComponentRepository themeComponentRepository;

  private final int initialThemeCounts = 10;
  private final int initialStylePerTheme = 5;
  private final int initialImagePerTheme = 4;

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(initialThemeCounts, initialStylePerTheme,
        initialImagePerTheme);
  }

  @AfterEach
  void tearDown() {
    themeDataGenerator.deleteTestData();
  }

  private List<ThemeImageRequest> getImageRequests() {
    return themeDataGenerator.initialComponentTypes.stream()
        .map(componentType -> ThemeImageRequest.builder()
            .componentTypeId(componentType.getComponentTypeId())
            .designComponentId(
                themeDataGenerator.initialDesignComponents.get(0).getDesignComponentId())
            .build()).toList();
  }

  private List<ThemeStyleRequest> getStyleRequests() {
    return themeDataGenerator.initialColorStyles.stream()
        .map(colorStyle -> ThemeStyleRequest.builder()
            .colorTypeId(colorStyle.getColorTypeId())
            .color("#ffffffff")
            .build()).toList();
  }

  @Test
  @Transactional
  @DisplayName("success test of creating new theme")
  public void createTheme_success() {
    // given
    CreateThemeRequest createThemeRequest = CreateThemeRequest.builder()
        .themeName("themeName")
        .images(getImageRequests())
        .styles(getStyleRequests())
        .isPublic(true)
        .versionName("versionName")
        .userEmail("test@test.com")
        .build();
    // when
    ThemeComponentDto res = themeManageService.createTheme(createThemeRequest);
    // then
    Optional<ThemeComponent> saved = themeComponentRepository.findById(res.getThemeComponentId());
    assertThat(saved.isPresent()).isTrue();
    assertThat(res.getImages()).hasSize(saved.get().getThemeImages().size());
    assertThat(res.getStyles()).hasSize(saved.get().getThemeStyles().size());
    assertThat(res.getVersionNumber()).isEqualTo("1");
  }

  @Test
  @Transactional
  @DisplayName("success test of updating theme")
  public void updateTheme_success() {
    // given
    List<ThemeImageRequest> images = getImageRequests();
    List<ThemeStyleRequest> styles = getStyleRequests();
    images = images.subList(0, initialImagePerTheme / 2);
    styles = styles.subList(0, initialStylePerTheme / 2);
    ThemeComponent toUpdate = themeDataGenerator.initialThemes.get(0);
    CreateThemeRequest updateThemeRequest = CreateThemeRequest.builder()
        .themeName("updated")
        .images(images)
        .styles(styles)
        .isPublic(true)
        .versionName("updated_version")
        .userEmail("test@test.com")
        .build();
    // when
    ThemeComponentDto res = themeManageService.updateTheme(toUpdate.getThemeComponentId(),
        updateThemeRequest);
    // then
    Optional<ThemeComponent> updated = themeComponentRepository.findById(res.getThemeComponentId());
    assertThat(updated.isPresent()).isTrue();
    assertThat(res.getImages()).hasSize(updated.get().getThemeImages().size())
        .hasSize(images.size());
    assertThat(res.getStyles()).hasSize(updated.get().getThemeStyles().size())
        .hasSize(styles.size());
    assertThat(res.getVersionName()).isEqualTo(updateThemeRequest.getVersionName());
  }

  @Test
  @DisplayName("success test of changing theme's state completed")
  public void markAsDone_success() {
    // given
    ThemeComponent toUpdate = themeDataGenerator.initialThemes.get(0);
    // when
    themeManageService.markAsDone(toUpdate.getThemeComponentId());
    // then
    Optional<ThemeComponent> updated = themeComponentRepository.findById(
        toUpdate.getThemeComponentId());
    assertThat(updated.isPresent()).isTrue();
    assertThat(updated.get().getIsDone()).isTrue();
  }

  @Test
  @DisplayName("success test of deleting theme")
  public void deleteTheme_success() {
    // given
    ThemeComponent toDelete = themeDataGenerator.initialThemes.get(0);
    // when
    themeManageService.deleteTheme(toDelete.getThemeComponentId());
    // then
    Optional<ThemeComponent> deleted = themeComponentRepository.findById(
        toDelete.getThemeComponentId());
    assertThat(deleted.isPresent()).isFalse();
  }
}