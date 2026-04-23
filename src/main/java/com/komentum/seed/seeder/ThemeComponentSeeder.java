package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ThemeComponentSeeder {

  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeImageRepository themeImageRepository;
  private final DesignComponentRepository designComponentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final Faker faker;

  private ThemeComponent generateOne(User author) {
    return ThemeComponent.builder()
        .themeName(faker.lorem().word())
        .versionName("1.1.1.1")
        .versionNumber("0")
        .isDone(true)
        .isPublic(true)
        .userEmail(author.getUserEmail())
        .build();
  }

  private List<ThemeImage> seedThemeImages(ThemeComponent themeComponent,
      List<DesignComponent> designComponents) {
    List<ComponentType> componentTypes = componentTypeRepository.findByPlatform(Platform.ANDROID);
    List<ThemeImage> themeImages = new ArrayList<>();
    for (int i = 0; i < componentTypes.size(); i++) {
      themeImages.add(ThemeImage.builder()
          .themeComponent(themeComponent)
          .componentType(componentTypes.get(i))
          .designComponent(designComponents.get(i % designComponents.size()))
          .build());
    }
    return themeImageRepository.saveAll(themeImages);
  }

  @Transactional
  public List<ThemeComponent> seedPerUser(int size, List<User> authors,
      List<DesignComponent> designComponents) {
    List<String> userEmailList = authors.stream()
        .map(User::getUserEmail)
        .toList();
    List<ThemeComponent> existing = themeComponentRepository.findByUserEmailIn(userEmailList);
    if (existing.size() >= size * authors.size()) {
      return existing;
    }
    List<ThemeComponent> themeComponents = new ArrayList<>();
    for (User author : authors) {
      for (int i = 0; i < size; i++) {
        themeComponents.add(generateOne(author));
      }
    }
    themeComponents = themeComponentRepository.saveAll(themeComponents);
    for (ThemeComponent themeComponent : themeComponents) {
      List<ThemeImage> themeImages = seedThemeImages(themeComponent, designComponents);
      for (ThemeImage themeImage : themeImages) {
        themeComponent.addThemeImage(themeImage);
      }
    }
    return themeComponents;
  }
}
