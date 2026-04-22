package com.komentum.theme.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeImageService;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
@EnableTestProfile
class ThemeRetrieveServiceTest {

  @Autowired
  private ThemeRetrieveService themeRetrieveService;

  @Autowired
  private ThemeImageService themeImageService;

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  private final int initialThemeCount = 10;
  private int initialStylePerTheme = 5;
  private int initialImagePerTheme = 4;

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(initialThemeCount);
    this.initialStylePerTheme = themeDataGenerator.initialColorStyles.size();
    this.initialImagePerTheme = themeDataGenerator.initialComponentTypes.size();
  }

  @AfterEach
  void tearDown() {
    themeDataGenerator.deleteTestData();
  }

  @Test
  @DisplayName("success test of retrieving all themes")
  void getAllThemes_success() {
    System.out.println("---start all themes");
    // given
    int pageNumber = 0;
    int pageSize = 5;
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    // when
    List<ThemeComponentDto> themeComponents = themeRetrieveService.getAllThemes(pageable);
    Map<Integer, String> previewImages = findPreviewImages(themeComponents);
    // then
    assertThat(themeComponents).hasSize(pageSize).allSatisfy(themeComponentDto -> {
      assertThat(themeComponentDto.getCreatedAt()).isNotNull();
      assertThat(themeComponentDto.getPreviewImageUrl())
          .isEqualTo(previewImages.get(themeComponentDto.getThemeComponentId()));
      assertThat(themeComponentDto.getStyles()).hasSize(initialStylePerTheme);
      assertThat(themeComponentDto.getImages()).hasSize(initialImagePerTheme);
    });
    System.out.println("---end all themes");
  }

  @Test
  @DisplayName("success test of retrieving theme by id")
  void getThemeById_success() {
    System.out.println("---start the theme");
    // given
    ThemeComponent toFind = themeDataGenerator.initialThemes.get(0);
    // when
    ThemeComponentDto founded = themeRetrieveService.getThemeById(toFind.getThemeComponentId());
    // then
    assertThat(founded.getThemeComponentId()).isEqualTo(toFind.getThemeComponentId());
    assertThat(founded.getCreatedAt()).isEqualTo(toFind.getCreatedAt());
    assertThat(founded.getPreviewImageUrl()).isEqualTo(
        findPreviewImageUrl(toFind.getThemeComponentId()));
    assertThat(founded.getStyles()).hasSize(initialStylePerTheme);
    assertThat(founded.getImages()).hasSize(initialImagePerTheme);
    System.out.println("---end the theme");
  }

  @Test
  @DisplayName("success test of retrieving theme by email")
  void getThemeByEmail_success() {
    System.out.println("---start theme by email");
    // given
    int pageNumber = 0;
    int pageSize = 10;
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    String userEmail = themeDataGenerator.userEmail;
    long counts = themeDataGenerator.initialThemes.stream()
        .filter(theme -> theme.getUserEmail().equals(userEmail)).count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getThemesByUserEmail(userEmail,
        pageable);
    // then
    assertThat(founded).hasSize((int) counts).allSatisfy(
        themeComponentDto -> assertThat(themeComponentDto.getUserEmail()).isEqualTo(userEmail));
    System.out.println("---end theme by email");
  }

  @Test
  @DisplayName("success test of retrieving public themes")
  void getPublicThemes_success() {
    System.out.println("---start public themes");
    // given
    int pageNumber = 0;
    int pageSize = 10;
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    long counts = themeDataGenerator.initialThemes.stream()
        .filter(ThemeComponent::getIsPublic).count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getPublicThemes(pageable);
    // then
    assertThat(founded).hasSize((int) counts)
        .allSatisfy(themeComponentDto -> assertThat(themeComponentDto.getIsPublic()).isTrue());
    System.out.println("---end public themes");
  }

  @Test
  @DisplayName("success test of retrieving completed themes")
  void getCompletedThemes_success() {
    System.out.println("---start completed themes");
    // given
    int pageNumber = 0;
    int pageSize = 10;
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    long counts = themeDataGenerator.initialThemes.stream().filter(ThemeComponent::getIsDone)
        .count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getCompletedThemes(pageable);
    // then
    assertThat(founded).hasSize((int) counts)
        .allSatisfy(themeComponentDto -> assertThat(themeComponentDto.getIsDone()).isTrue());
    System.out.println("---end completed themes");
  }

  @Test
  @DisplayName("success test of retrieving completed themes by user")
  void getCompletedThemesByUser_success() {
    System.out.println("---start completed themes by user");
    // given
    int pageNumber = 0;
    int pageSize = 10;
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    String userEmail = themeDataGenerator.userEmail;
    long counts = themeDataGenerator.initialThemes.stream()
        .filter(ThemeComponent::getIsDone)
        .filter(theme -> theme.getUserEmail().equals(userEmail)).count();
    // when
    List<ThemeComponentDto> founded = themeRetrieveService.getCompletedThemesByUser(userEmail,
        pageable);
    // then
    assertThat(founded).hasSize((int) counts).allSatisfy(themeComponentDto -> {
      assertThat(themeComponentDto.getIsDone()).isTrue();
      assertThat(themeComponentDto.getUserEmail()).isEqualTo(userEmail);
    });
    System.out.println("---end completed themes by user");
  }

  private Map<Integer, String> findPreviewImages(List<ThemeComponentDto> themeComponents) {
    return themeImageService.findThemePreviewImages(
        themeComponents.stream()
            .map(ThemeComponentDto::getThemeComponentId)
            .toList());
  }

  private String findPreviewImageUrl(Integer themeComponentId) {
    return themeImageService.findThemePreviewImages(List.of(themeComponentId))
        .get(themeComponentId);
  }
}
