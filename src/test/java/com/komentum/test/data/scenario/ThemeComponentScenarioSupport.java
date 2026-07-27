package com.komentum.test.data.scenario;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.seed.seeder.ThemeComponentSeeder;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.enums.ThemeType;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeComponentScenarioSupport {

  private final ThemeComponentSeeder themeComponentSeeder;
  private final ThemeMetaDataScenarioSupport themeMetaDataScenarioSupport;

  public record ThemeComponentScenarioResult(
      List<ThemeComponent> themeComponents,
      ThemeComponent defaultTheme
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
    private boolean shouldCreateDefaultTheme = false;

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

    public ThemeComponentScenarioBuilder withDefaultTheme() {
      shouldCreateDefaultTheme = true;
      return this;
    }

    public ThemeComponentScenarioResult build() {
      // generate theme metadata
      themeMetaDataScenarioSupport.builder()
          .withAll()
          .build();
      // generate theme components
      if (countPerUser <= 0) {
        throw new IllegalArgumentException("countPerUser must be bigger than 0");
      }
      List<ThemeComponent> themeComponents = themeComponentSeeder.seedPerUser(countPerUser, users,
          designComponents);
      // generate default theme
      ThemeComponent defaultTheme = null;
      if (shouldCreateDefaultTheme) {
        defaultTheme = themeComponentSeeder.seedOne(users.get(0), designComponents,
            ThemeType.DEFAULT);
        themeComponents.add(defaultTheme);
      }
      // convert data to result
      return new ThemeComponentScenarioResult(themeComponents, defaultTheme);
    }
  }
}
