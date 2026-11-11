package com.komentum.seed;

import com.komentum.designcomponent.service.seeder.ColorStyleSeeder;
import com.komentum.designcomponent.service.seeder.ComponentTypeSeeder;
import com.komentum.designcomponent.service.seeder.PlatformColorStyleSeeder;
import com.komentum.designcomponent.service.seeder.PlatformComponentTypeSeeder;
import com.komentum.seed.seeder.UserSeeder;
import com.komentum.theme.core.service.seeder.DefaultThemeSeeder;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile({"prod"})
@RequiredArgsConstructor
public class DefaultDataInitializer {

  private final UserSeeder userSeeder;
  private final DefaultThemeSeeder defaultThemeSeeder;
  private final ColorStyleSeeder colorStyleSeeder;
  private final ComponentTypeSeeder componentTypeSeeder;
  private final PlatformComponentTypeSeeder platformComponentTypeSeeder;
  private final PlatformColorStyleSeeder platformColorStyleSeeder;

  @EventListener(ApplicationReadyEvent.class)
  public void createDefaultData() {
    // generate or retrieve root user
    User rootUser = userSeeder.createOrRetrieveRootUser();
    // generate color style, component type
    colorStyleSeeder.upsertColorStyleSeed();
    componentTypeSeeder.upsertComponentType();
    // generate platform color style, platform component type
    platformColorStyleSeeder.upsertPlatformColorStyle();
    platformComponentTypeSeeder.upsertPlatformComponentType();
    // generate default themes
    defaultThemeSeeder.seedDefaultThemes(rootUser.getPublicUserId());
  }
}
