package com.komentum.theme.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.service.ThemeManageService;
import com.komentum.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  private final int initialThemeCounts = 10;
  private int initialStylePerTheme;
  private int initialImagePerTheme;

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(initialThemeCounts);
    this.initialStylePerTheme = themeDataGenerator.initialColorStyles.size();
    this.initialImagePerTheme = themeDataGenerator.initialComponentTypes.size();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    themeDataGenerator.deleteTestData();
  }

  @Test
  @Transactional
  @DisplayName("success test of creating new theme")
  public void createTheme_success() {
    // given
    User client = userScenarioSupport.builder().withUsers(1).build().getFirstUser();
    // when
    ThemeComponent res = themeManageService.createNewTheme(client);
    // then
    Optional<ThemeComponent> saved = themeComponentRepository.findById(res.getThemeComponentId());
    assertThat(saved.isPresent()).isTrue();
    assertThat(res.getVersionNumber()).isEqualTo("1");
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
    User owner = toDelete.getUser();
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .userEmail(owner.getUserEmail())
        .publicUserId(owner.getPublicUserId())
        .userRole(owner.getRole())
        .build();
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()));
    // when
    themeManageService.deleteTheme(toDelete.getThemeComponentId());
    // then
    Optional<ThemeComponent> deleted = themeComponentRepository.findById(
        toDelete.getThemeComponentId());
    assertThat(deleted.isPresent()).isFalse();
  }
}
