package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ThemeBoardSeeder {

  private final ThemeBoardRepository themeBoardRepository;
  private final Faker faker;

  private ThemeBoard generateOne(ThemeComponent themeComponent, Post post) {
    return ThemeBoard.builder()
        .post(post)
        .themeComponent(themeComponent)
        .build();
  }

  @Transactional
  public List<ThemeBoard> seedData(List<ThemeComponent> themeComponents, List<Post> posts) {
    List<ThemeBoard> themeBoards = new ArrayList<>();
    int size = Math.min(themeComponents.size(), posts.size());
    for (int i = 0; i < size; i++) {
      ThemeComponent component = themeComponents.get(i);
      Post post = posts.get(i);
      if (!themeBoardRepository.existsByThemeComponentAndPost(component, post)) {
        themeBoards.add(generateOne(component, post));
      }
    }
    return themeBoardRepository.saveAll(themeBoards);
  }
}
