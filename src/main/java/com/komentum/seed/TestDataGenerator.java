package com.komentum.seed;

import com.komentum.post.domain.Post;
import com.komentum.seed.seeder.CommentSeeder;
import com.komentum.seed.seeder.DesignBoardSeeder;
import com.komentum.seed.seeder.DesignComponentSeeder;
import com.komentum.seed.seeder.PostSeeder;
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
@Profile("dev")
@RequiredArgsConstructor
public class TestDataGenerator {

  private final UserSeeder userSeeder;
  private final PostSeeder postSeeder;
  private final CommentSeeder commentSeeder;
  private final PreferSeeder preferSeeder;
  private final ThemeComponentSeeder themeComponentSeeder;
  private final DesignComponentSeeder designComponentSeeder;
  private final ThemeBoardSeeder themeBoardSeeder;
  private final DesignBoardSeeder designBoardSeeder;

  @PostConstruct
  @Transactional
  public void init() {
    List<User> users = userSeeder.seedData(10); // 10
    List<Post> posts = postSeeder.seedPerUser(3, users); // 30
    commentSeeder.seedPerPost(5, posts, users); // 150
    preferSeeder.seedPerPost(5, posts, users); // 150
    List<ThemeComponent> themeComponents = themeComponentSeeder.seedPerUser(4, users); // 40
    List<DesignComponent> designComponents = designComponentSeeder.seedPeruser(4, users); // 40
    themeBoardSeeder.seedData(themeComponents,
        posts.subList(10, 20));
    designBoardSeeder.seedData(designComponents,
        posts.subList(20, 30));
  }
}
