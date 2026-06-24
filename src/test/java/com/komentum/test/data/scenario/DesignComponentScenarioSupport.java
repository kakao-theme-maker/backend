package com.komentum.test.data.scenario;

import com.komentum.seed.seeder.DesignComponentSeeder;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DesignComponentScenarioSupport {

  private final DesignComponentSeeder designComponentSeeder;

  public record DesignComponentScenarioResult(
      List<DesignComponent> designComponents
  ) {

  }

  public DesignComponentScenarioBuilder builder(List<User> users) {
    return new DesignComponentScenarioBuilder(users);
  }

  public class DesignComponentScenarioBuilder {

    private int countPerUser;
    List<User> users;

    public DesignComponentScenarioBuilder(List<User> users) {
      this.users = users;
    }

    public DesignComponentScenarioBuilder withCountPerUser(int countPerUser) {
      this.countPerUser = countPerUser;
      return this;
    }

    public DesignComponentScenarioResult build() {
      // generate design components
      if (countPerUser <= 0) {
        throw new IllegalArgumentException("countPerUser must bigger than 0");
      }
      List<DesignComponent> designComponents = designComponentSeeder.seedPeruser(countPerUser,
          users);
      // convert data to result
      return new DesignComponentScenarioResult(designComponents);
    }
  }
}
