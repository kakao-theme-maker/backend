package com.komentum.seed.seeder;

import com.github.javafaker.Faker;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.repository.ColorStyleRepository;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.enums.ThemeType;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.user.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ThemeComponentSeeder {

  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeImageRepository themeImageRepository;
  private final ThemeStyleRepository themeStyleRepository;
  private final DesignComponentRepository designComponentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final ColorStyleRepository colorStyleRepository;
  private final Faker faker;

  private ThemeComponent generateOne(User author, ThemeType themeType, String themeCode) {
    return ThemeComponent.builder()
        .themeName(faker.lorem().word())
        .versionName("1.1.1.1")
        .versionNumber("0")
        .isDone(true)
        .isPublic(true)
        .themeType(themeType)
        .themeCode(themeCode)
        .userEmail(author.getUserEmail())
        .build();
  }

  private List<ThemeImage> seedThemeImages(ThemeComponent themeComponent,
      List<DesignComponent> designComponents) {
    List<ComponentType> componentTypes = componentTypeRepository.findAll();
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

  private List<ThemeStyle> seedThemeStyles(ThemeComponent themeComponent) {
    List<ColorStyle> colorStyles = colorStyleRepository.findAll();
    List<ThemeStyle> themeStyles = new ArrayList<>();
    for (ColorStyle colorStyle : colorStyles) {
      themeStyles.add(ThemeStyle.builder()
          .colorStyle(colorStyle)
          .themeComponent(themeComponent)
          .color(faker.color().name())
          .build());
    }
    return themeStyleRepository.saveAll(themeStyles);
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
        ThemeType themeType = ThemeType.USER;
        themeComponents.add(
            seedOne(author, designComponents, themeType, UUID.randomUUID().toString()));
      }
    }
    return themeComponents;
  }

  @Transactional
  public ThemeComponent seedOne(User author, List<DesignComponent> designComponents) {
    return seedOne(author, designComponents, ThemeType.USER, UUID.randomUUID().toString());
  }

  @Transactional
  public ThemeComponent seedOne(User author, List<DesignComponent> designComponents,
      ThemeType themeType, String themeCode) {
    ThemeComponent themeComponent = themeComponentRepository.save(
        generateOne(author, themeType, themeCode));
    List<ThemeImage> themeImages = seedThemeImages(themeComponent, designComponents);
    List<ThemeStyle> themeStyles = seedThemeStyles(themeComponent);
    for (ThemeImage themeImage : themeImages) {
      themeComponent.addThemeImage(themeImage);
    }
    for (ThemeStyle themeStyle : themeStyles) {
      themeComponent.addThemeStyle(themeStyle);
    }
    return themeComponent;
  }
}
