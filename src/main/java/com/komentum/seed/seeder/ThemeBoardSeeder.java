package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ThemeBoardSeeder {

  private final ThemeBoardRepository themeBoardRepository;
  private final ThemeImageRepository themeImageRepository;
  private final PostSeeder postSeeder;
  private final Faker faker;
  private final ThemeImageService themeImageService;

  public static record ThemeBoardSeedResult(
      List<ThemeBoard> themeBoards,
      List<Post> posts
  ) {

  }

  private ThemeBoard generateOne(ThemeComponent themeComponent, Post post) {
    return ThemeBoard.builder()
        .post(post)
        .themeComponent(themeComponent)
        .build();
  }

  @Transactional
  public ThemeBoardSeedResult seedData(List<ThemeComponent> themeComponents) {
    List<ThemeBoard> themeBoards = new ArrayList<>();
    List<Post> posts = new ArrayList<>();
    int size = themeComponents.size();
    for (int i = 0; i < size; i++) {
      ThemeComponent component = themeComponents.get(i);
      User author = component.getUser();
      String previewImageFileName = themeImageService.findThemePreviewFileName(
          component.getThemeComponentId());
      Post post = postSeeder.createOne(
          author,
          previewImageFileName,
          PostType.THEME_BOARD
      );
      posts.add(post);
      if (!themeBoardRepository.existsByThemeComponentAndPost(component, post)) {
        themeBoards.add(generateOne(component, post));
      }
    }
    List<ThemeBoard> saved = themeBoardRepository.saveAll(themeBoards);
    return new ThemeBoardSeedResult(saved, posts);
  }
}
