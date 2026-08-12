package com.komentum.test.data.scenario;

import com.komentum.seed.seeder.UserSeeder;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserScenarioSupport {

  private final UserSeeder userSeeder;

  public record UserScenarioResult(
      List<User> users,
      User rootUser
  ) {

    public User getFirstUser() {
      return users.get(0);
    }
  }

  public UserScenarioBuilder builder() {
    return new UserScenarioBuilder();
  }

  public class UserScenarioBuilder {

    private int userCount;
    private boolean isRootUserExists = false;

    public UserScenarioBuilder withUsers(int userCount) {
      this.userCount = userCount;
      return this;
    }

    public UserScenarioBuilder withRootUser() {
      this.isRootUserExists = true;
      return this;
    }

    public UserScenarioResult build() {
      List<User> users = new ArrayList<>();
      User rootUser = null;
      if (userCount > 0) {
        users = userSeeder.seedData(userCount);
      }
      if (isRootUserExists) {
        rootUser = userSeeder.createOrRetrieveRootUser();
      }
      return new UserScenarioResult(users, rootUser);
    }
  }
}
