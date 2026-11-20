package com.komentum.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.service.seeder.ColorStyleSeeder;
import com.komentum.designcomponent.service.seeder.ComponentTypeSeeder;
import com.komentum.designcomponent.service.seeder.PlatformColorStyleSeeder;
import com.komentum.designcomponent.service.seeder.PlatformComponentTypeSeeder;
import com.komentum.seed.seeder.CommentSeeder;
import com.komentum.seed.seeder.DesignBoardSeeder;
import com.komentum.seed.seeder.DesignComponentSeeder;
import com.komentum.seed.seeder.PreferSeeder;
import com.komentum.seed.seeder.ThemeBoardSeeder;
import com.komentum.seed.seeder.ThemeComponentSeeder;
import com.komentum.seed.seeder.UserSeeder;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.service.seeder.DefaultThemeSeeder;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

class DevDataGeneratorIntegrationTest {

  private final UserSeeder userSeeder = mock(UserSeeder.class);
  private final CommentSeeder commentSeeder = mock(CommentSeeder.class);
  private final PreferSeeder preferSeeder = mock(PreferSeeder.class);
  private final ThemeComponentSeeder themeComponentSeeder = mock(ThemeComponentSeeder.class);
  private final DesignComponentSeeder designComponentSeeder = mock(DesignComponentSeeder.class);
  private final ThemeBoardSeeder themeBoardSeeder = mock(ThemeBoardSeeder.class);
  private final DesignBoardSeeder designBoardSeeder = mock(DesignBoardSeeder.class);
  private final ComponentTypeSeeder componentTypeSeeder = mock(ComponentTypeSeeder.class);
  private final ColorStyleSeeder colorStyleSeeder = mock(ColorStyleSeeder.class);
  private final PlatformComponentTypeSeeder platformComponentTypeSeeder =
      mock(PlatformComponentTypeSeeder.class);
  private final PlatformColorStyleSeeder platformColorStyleSeeder =
      mock(PlatformColorStyleSeeder.class);
  private final DefaultThemeSeeder defaultThemeSeeder = mock(DefaultThemeSeeder.class);

  private final User rootUser = User.builder().publicUserId("root-user").build();
  private final List<User> sampleUsers = Collections.nCopies(10, mock(User.class));
  private final List<DesignComponent> designComponents =
      Collections.nCopies(110, mock(DesignComponent.class));
  private final List<ThemeComponent> themeComponents =
      Collections.nCopies(110, mock(ThemeComponent.class));

  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withInitializer(context -> {
        context.getEnvironment().setActiveProfiles("dev");
        context.getEnvironment().getPropertySources()
            .remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);
        context.getEnvironment().getPropertySources()
            .remove(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME);
      })
      .withUserConfiguration(DevDataGenerator.class)
      .withBean(UserSeeder.class, () -> userSeeder)
      .withBean(CommentSeeder.class, () -> commentSeeder)
      .withBean(PreferSeeder.class, () -> preferSeeder)
      .withBean(ThemeComponentSeeder.class, () -> themeComponentSeeder)
      .withBean(DesignComponentSeeder.class, () -> designComponentSeeder)
      .withBean(ThemeBoardSeeder.class, () -> themeBoardSeeder)
      .withBean(DesignBoardSeeder.class, () -> designBoardSeeder)
      .withBean(ComponentTypeSeeder.class, () -> componentTypeSeeder)
      .withBean(ColorStyleSeeder.class, () -> colorStyleSeeder)
      .withBean(PlatformComponentTypeSeeder.class, () -> platformComponentTypeSeeder)
      .withBean(PlatformColorStyleSeeder.class, () -> platformColorStyleSeeder)
      .withBean(DefaultThemeSeeder.class, () -> defaultThemeSeeder);

  /**
   * 초기화 흐름을 실행할 수 있도록 루트 사용자와 기존 샘플 크기에 맞는 반환값을 준비한다.
   */
  @BeforeEach
  void setUpSeeders() {
    when(userSeeder.createOrRetrieveRootUser()).thenReturn(rootUser);
    when(userSeeder.seedData(10)).thenReturn(sampleUsers);
    when(designComponentSeeder.seedPeruser(eq(10), anyList())).thenReturn(designComponents);
    when(themeComponentSeeder.seedPerUser(eq(10), anyList(), eq(designComponents)))
        .thenReturn(themeComponents);
  }

  /**
   * 서버 환경변수로 샘플을 비활성화해도 공통 초기화가 실행되는지 검증한다.
   */
  @Test
  @DisplayName("dev에서 환경변수 false이면 기본 초기화만 실행하고 개발 샘플을 생성하지 않는다")
  void devWithSampleDataDisabled_initializesDefaultsOnly() {
    contextRunner
        .withInitializer(context -> {
          context.getEnvironment().getPropertySources().addFirst(
              new SystemEnvironmentPropertySource("sample-data-test-environment",
                  Map.of("APP_SEED_SAMPLE_DATA_ENABLED", "false")));
        })
        .run(context -> {
          assertThat(context).hasNotFailed().hasSingleBean(DevDataGenerator.class);
          verifyDefaultInitialization();
          verify(userSeeder, never()).seedData(anyInt());
          verifyNoInteractions(designComponentSeeder, themeComponentSeeder, themeBoardSeeder,
              designBoardSeeder, commentSeeder, preferSeeder);
        });
  }

  /**
   * true 설정과 설정 생략 시 기존 개발 샘플 생성이 유지되는지 검증한다. null은 설정 생략을 뜻한다.
   */
  @ParameterizedTest
  @NullSource
  @ValueSource(strings = "true")
  @DisplayName("dev에서 설정이 true이거나 생략되면 기본 초기화와 개발 샘플 생성을 실행한다")
  void devWithSampleDataEnabledOrOmitted_initializesDefaultsAndSamples(String configuredValue) {
    ApplicationContextRunner runner = contextRunner;
    if (configuredValue != null) {
      runner = runner.withPropertyValues("app.seed.sample-data-enabled=" + configuredValue);
    }
    runner.run(context -> {
      assertThat(context).hasNotFailed().hasSingleBean(DevDataGenerator.class);
      verifyDefaultInitialization();
      verifySampleGeneration();
    });
  }

  /**
   * dev 외 프로필에서는 개발용 초기화 빈이 생성되지 않는지 검증한다.
   */
  @Test
  @DisplayName("test 프로필에서는 DevDataGenerator를 등록하거나 시더를 호출하지 않는다")
  void nonDevProfile_doesNotInitializeSeeders() {
    contextRunner
        .withInitializer(context -> context.getEnvironment().setActiveProfiles("test"))
        .withPropertyValues("app.seed.sample-data-enabled=true")
        .run(context -> {
          assertThat(context).hasNotFailed().doesNotHaveBean(DevDataGenerator.class);
          verifyNoInteractions(userSeeder, commentSeeder, preferSeeder, themeComponentSeeder,
              designComponentSeeder, themeBoardSeeder, designBoardSeeder, componentTypeSeeder,
              colorStyleSeeder, platformComponentTypeSeeder, platformColorStyleSeeder,
              defaultThemeSeeder);
        });
  }

  /**
   * 샘플 활성화 여부와 관계없이 필요한 루트 사용자, 메타데이터와 기본 테마 초기화를 확인한다.
   */
  private void verifyDefaultInitialization() {
    verify(userSeeder).createOrRetrieveRootUser();
    verify(componentTypeSeeder).upsertComponentType();
    verify(colorStyleSeeder).upsertColorStyleSeed();
    verify(platformComponentTypeSeeder).upsertPlatformComponentType();
    verify(platformColorStyleSeeder).upsertPlatformColorStyle();
    verify(defaultThemeSeeder).seedDefaultThemes(rootUser.getPublicUserId());
  }

  /**
   * 기존 샘플 생성 대상과 전달되는 사용자, 에셋, 테마 목록을 확인한다.
   */
  private void verifySampleGeneration() {
    List<User> users = new ArrayList<>(sampleUsers);
    users.add(rootUser);

    verify(userSeeder).seedData(10);
    verify(designComponentSeeder).seedPeruser(10, users);
    verify(themeComponentSeeder).seedPerUser(10, users, designComponents);
    verify(themeBoardSeeder).seedData(themeComponents.subList(0, 90));
    verify(designBoardSeeder).seedData(designComponents.subList(0, 90));
    verify(commentSeeder).seedPerPost(5, users);
    verify(preferSeeder).seedPerPost(5, users, 0.5);
  }
}
