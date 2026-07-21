package com.komentum.seed;

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
import com.komentum.user.domain.User;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"dev"})
@RequiredArgsConstructor
public class DevDataGenerator {

  private final UserSeeder userSeeder;
  private final CommentSeeder commentSeeder;
  private final PreferSeeder preferSeeder;
  private final ThemeComponentSeeder themeComponentSeeder;
  private final DesignComponentSeeder designComponentSeeder;
  private final ThemeBoardSeeder themeBoardSeeder;
  private final DesignBoardSeeder designBoardSeeder;
  private final ComponentTypeSeeder componentTypeSeeder;
  private final ColorStyleSeeder colorStyleSeeder;
  private final PlatformComponentTypeSeeder platformComponentTypeSeeder;
  private final PlatformColorStyleSeeder platformColorStyleSeeder;

  @PostConstruct
  @Transactional
  public void init() {
    List<User> users = new ArrayList<>(userSeeder.seedData(10));
    users.add(userSeeder.createOrRetrieveRootUser()); // 11
    // seed component type, color style, platform component type, platform color style
    componentTypeSeeder.upsertComponentType();
    colorStyleSeeder.upsertColorStyleSeed();
    platformComponentTypeSeeder.upsertPlatformComponentType();
    platformColorStyleSeeder.upsertPlatformColorStyle();
    // seed design component, theme component
    List<DesignComponent> designComponents = designComponentSeeder.seedPeruser(10,
        users); // 110
    List<ThemeComponent> themeComponents = themeComponentSeeder.seedPerUser(10, users,
        designComponents); // 110
    // seed theme board, design board, comment, prefer
    themeBoardSeeder.seedData(themeComponents.subList(0, 90)); //
    designBoardSeeder.seedData(designComponents.subList(0, 90)); // 100
    commentSeeder.seedPerPost(5, users); // 550
    preferSeeder.seedPerPost(5, users, 0.5); // 550
  }
}
