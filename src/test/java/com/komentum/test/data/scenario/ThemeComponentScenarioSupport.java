package com.komentum.test.data.scenario;

import com.komentum.seed.seeder.ThemeComponentSeeder;
import com.komentum.theme.component.service.ColorStyleSeeder;
import com.komentum.theme.component.service.ComponentTypeSeeder;
import com.komentum.theme.theme.domain.ThemeComponent;
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

  public ThemeComponentScenarioBuilder builder(List<User> users) {
    return new ThemeComponentScenarioBuilder().builder(users);
  }

  public class ThemeComponentScenarioBuilder {

    private List<User> users;
    private int countPerUser;

    public ThemeComponentScenarioBuilder builder(List<User> users) {
      this.users = users;
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
      List<ThemeComponent> themeComponents = themeComponentSeeder.seedPerUser(countPerUser, users);
      // convert data to result
      return new ThemeComponentScenarioResult(themeComponents);
    }
  }
}
