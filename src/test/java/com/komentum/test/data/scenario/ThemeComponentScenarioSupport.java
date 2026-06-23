package com.komentum.test.data.scenario;

import com.komentum.seed.seeder.ThemeComponentSeeder;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.service.ColorStyleSeeder;
import com.komentum.designcomponent.service.ComponentTypeSeeder;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeComponentScenarioSupport {

  private final ThemeComponentSeeder themeComponentSeeder;
  private final ComponentTypeSeeder componentTypeSeeder;
  private final ColorStyleSeeder colorStyleSeeder;

  public record ThemeComponentScenarioResult(
      List<ThemeComponent> themeComponents
  ) {

  }

  public ThemeComponentScenarioBuilder builder(List<User> users,
      List<DesignComponent> designComponents) {
    return new ThemeComponentScenarioBuilder().builder(users, designComponents);
  }

  public class ThemeComponentScenarioBuilder {

    private List<User> users;
    private int countPerUser;
    private List<DesignComponent> designComponents;

    public ThemeComponentScenarioBuilder builder(List<User> users,
        List<DesignComponent> designComponents) {
      this.users = users;
      this.designComponents = designComponents;
      return this;
    }

    public ThemeComponentScenarioBuilder withCountPerUser(int countPerUser) {
      this.countPerUser = countPerUser;
      return this;
    }

    public ThemeComponentScenarioResult build() {
      // generate color style & component type
      componentTypeSeeder.upsertComponentType();
      colorStyleSeeder.upsertColorStyleSeed();
      // generate theme components
      if (countPerUser <= 0) {
        throw new IllegalArgumentException("countPerUser must be bigger than 0");
      }
      List<ThemeComponent> themeComponents = themeComponentSeeder.seedPerUser(countPerUser, users,
          designComponents);
      // convert data to result
      return new ThemeComponentScenarioResult(themeComponents);
    }
  }
}
