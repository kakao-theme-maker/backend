package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.global.utils.FileManager;
import com.komentum.post.consts.ThemeBoardConsts;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.repository.ThemeBoardRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
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
  private final UserRepository userRepository;
  private final PostSeeder postSeeder;
  private final FileManager fileManager;
  private final Faker faker;

  private ThemeBoard generateOne(ThemeComponent themeComponent, Post post) {
    return ThemeBoard.builder()
        .post(post)
        .themeComponent(themeComponent)
        .build();
  }

  @Transactional
  public List<ThemeBoard> seedData(List<ThemeComponent> themeComponents) {
    List<ThemeBoard> themeBoards = new ArrayList<>();
    int size = themeComponents.size();
    for (int i = 0; i < size; i++) {
      ThemeComponent component = themeComponents.get(i);
      User author = userRepository.findByUserEmail(component.getUserEmail())
          .orElseThrow(() -> new IllegalArgumentException(
              "user with " + component.getUserEmail() + " doesn't exists"));
      ThemeImage iconThemeImage = component.getThemeImages().stream()
          .filter(image -> {
            String componentName = image.getComponentType().getComponentName();
            if (componentName.equals(ThemeBoardManagementFacade.DEFAULT_COMPONENT_TYPE_NAME)) {
              return true;
            }
            return false;
          })
          .findFirst()
          .orElseThrow(() -> new IllegalStateException(
              "Default theme image not found for theme component : "
                  + component.getThemeComponentId()));
      Post post = postSeeder.createOne(
          author,
          fileManager.convertUrlToFileName(iconThemeImage.getDesignComponent().getImageUrl())
      );
      if (!themeBoardRepository.existsByThemeComponentAndPost(component, post)) {
        themeBoards.add(generateOne(component, post));
      }
    }
    return themeBoardRepository.saveAll(themeBoards);
  }
}
