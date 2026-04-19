package com.komentum.test.data.scenario;

import com.komentum.seed.seeder.UserSeeder;
import com.komentum.user.domain.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserScenarioSupport {

  private final UserSeeder userSeeder;

  public record UserScenarioResult(
      List<User> users
  ) {

  }

  public UserScenarioBuilder builder() {
    return new UserScenarioBuilder();
  }

  public class UserScenarioBuilder {

    private int userCount;

    public UserScenarioBuilder withUsers(int userCount) {
      this.userCount = userCount;
      return this;
    }

    public UserScenarioResult build() {
      List<User> users = userSeeder.seedData(userCount);
      return new UserScenarioResult(users);
    }
  }
}
