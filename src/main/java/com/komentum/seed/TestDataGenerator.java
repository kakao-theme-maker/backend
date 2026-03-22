package com.komentum.seed;

import com.komentum.seed.seeder.CommentSeeder;
import com.komentum.seed.seeder.ComponentTypeSeeder;
import com.komentum.seed.seeder.DesignBoardSeeder;
import com.komentum.seed.seeder.DesignComponentSeeder;
import com.komentum.seed.seeder.PreferSeeder;
import com.komentum.seed.seeder.ThemeBoardSeeder;
import com.komentum.seed.seeder.ThemeComponentSeeder;
import com.komentum.seed.seeder.UserSeeder;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"dev"})
@RequiredArgsConstructor
public class TestDataGenerator {

  private final UserSeeder userSeeder;
  private final CommentSeeder commentSeeder;
  private final PreferSeeder preferSeeder;
  private final ThemeComponentSeeder themeComponentSeeder;
  private final DesignComponentSeeder designComponentSeeder;
  private final ThemeBoardSeeder themeBoardSeeder;
  private final DesignBoardSeeder designBoardSeeder;
  private final ComponentTypeSeeder componentTypeSeeder;

  @PostConstruct
  @Transactional
  public void init() {
    List<User> users = userSeeder.seedData(10); // 10
    userSeeder.createOrRetrieveRootUser();
    componentTypeSeeder.seedData();
    List<DesignComponent> designComponents = designComponentSeeder.seedPeruser(10,
        users); // 100
    List<ThemeComponent> themeComponents = themeComponentSeeder.seedPerUser(10, users); // 100
    themeBoardSeeder.seedData(themeComponents.subList(0, 90)); // 100
    designBoardSeeder.seedData(designComponents.subList(0, 90)); // 100
    commentSeeder.seedPerPost(5, users); // 150
    preferSeeder.seedPerPost(5, users); // 150
    userSeeder.createOrRetrieveRootUser();
  }
}
