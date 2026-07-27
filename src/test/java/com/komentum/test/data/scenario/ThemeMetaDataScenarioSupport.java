package com.komentum.test.data.scenario;

import com.komentum.designcomponent.service.seeder.ColorStyleSeeder;
import com.komentum.designcomponent.service.seeder.ComponentTypeSeeder;
import com.komentum.designcomponent.service.seeder.PlatformColorStyleSeeder;
import com.komentum.designcomponent.service.seeder.PlatformComponentTypeSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeMetaDataScenarioSupport {

  private final ComponentTypeSeeder componentTypeSeeder;
  private final ColorStyleSeeder colorStyleSeeder;
  private final PlatformComponentTypeSeeder platformComponentTypeSeeder;
  private final PlatformColorStyleSeeder platformColorStyleSeeder;

  public ThemeMetaDataScenarioBuilder builder() {
    return new ThemeMetaDataScenarioBuilder();
  }

  public class ThemeMetaDataScenarioBuilder {

    boolean shouldSeedComponentType = false;
    boolean shouldSeedColorStyle = false;
    boolean shouldSeedPlatformComponentType = false;
    boolean shouldSeedPlatformColorStyle = false;

    public ThemeMetaDataScenarioBuilder withComponentType() {
      shouldSeedComponentType = true;
      return this;
    }

    public ThemeMetaDataScenarioBuilder withColorStyle() {
      shouldSeedColorStyle = true;
      return this;
    }

    public ThemeMetaDataScenarioBuilder withPlatformComponentType() {
      shouldSeedPlatformComponentType = true;
      return this;
    }

    public ThemeMetaDataScenarioBuilder withPlatformColorStyle() {
      shouldSeedPlatformColorStyle = true;
      return this;
    }

    public ThemeMetaDataScenarioBuilder withAll() {
      withComponentType();
      withColorStyle();
      withPlatformComponentType();
      withPlatformColorStyle();
      return this;
    }

    public void build() {
      if (shouldSeedComponentType) {
        componentTypeSeeder.upsertComponentType();
      }
      if (shouldSeedColorStyle) {
        colorStyleSeeder.upsertColorStyleSeed();
      }
      if (shouldSeedPlatformComponentType) {
        platformComponentTypeSeeder.upsertPlatformComponentType();
      }
      if (shouldSeedPlatformColorStyle) {
        platformColorStyleSeeder.upsertPlatformColorStyle();
      }
    }
  }
}
