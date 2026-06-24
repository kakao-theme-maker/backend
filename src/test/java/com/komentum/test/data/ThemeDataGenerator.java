package com.komentum.test.data;

import com.github.javafaker.Faker;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.repository.ColorStyleRepository;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.designcomponent.service.ColorStyleSeeder;
import com.komentum.designcomponent.service.ComponentTypeSeeder;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeImageRequest;
import com.komentum.theme.core.dto.ThemeStyleRequest;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThemeDataGenerator {

  @Autowired
  private ComponentTypeSeeder componentTypeSeeder;

  @Autowired
  private ColorStyleSeeder colorStyleSeeder;

  @Autowired
  private ThemeComponentRepository themeComponentRepository;

  @Autowired
  private ThemeImageRepository themeImageRepository;

  @Autowired
  private ThemeStyleRepository themeStyleRepository;

  @Autowired
  private ColorStyleRepository colorStyleRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private UserDataGenerator userDataGenerator;

  private final Faker faker = new Faker();
  public String userEmail = "test@test.com";
  public List<ThemeComponent> initialThemes = new ArrayList<>();
  public List<ColorStyle> initialColorStyles = new ArrayList<>();
  public List<ComponentType> initialComponentTypes = new ArrayList<>();
  public List<DesignComponent> initialDesignComponents = new ArrayList<>();

  public void generateTestData(int themeCount) {
    initialColorStyles = generateColorStyles();
    initialComponentTypes = generateComponentTypes();
    initialDesignComponents = generateDesignComponents(initialComponentTypes.size());
    initialThemes = generateThemeComponents(themeCount, initialColorStyles, initialComponentTypes,
        initialDesignComponents);
  }

  public List<ThemeImageRequest> getImageRequests() {
    return initialComponentTypes.stream()
        .map(componentType -> ThemeImageRequest.builder()
            .componentTypeId(componentType.getComponentTypeId())
            .designComponentId(
                initialDesignComponents.get(0).getDesignComponentId())
            .build()).toList();
  }

  public List<ThemeStyleRequest> getStyleRequests() {
    return initialColorStyles.stream()
        .map(colorStyle -> ThemeStyleRequest.builder()
            .colorStyleId(colorStyle.getColorStyleId())
            .color("#ffffffff")
            .build()).toList();
  }

  public void deleteTestData() {
    themeImageRepository.deleteAll();
    themeStyleRepository.deleteAll();
    themeComponentRepository.deleteAll();
    designComponentRepository.deleteAll();
    colorStyleRepository.deleteAll();
    componentTypeRepository.deleteAll();
  }

  public List<ColorStyle> generateColorStyles() {
    colorStyleSeeder.upsertColorStyleSeed();
    return colorStyleRepository.findAll();
  }

  public List<ComponentType> generateComponentTypes() {
    componentTypeSeeder.upsertComponentType();
    return componentTypeRepository.findAll();
  }

  public List<DesignComponent> generateDesignComponents(int size) {
    List<DesignComponent> designComponents = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      designComponents.add(DesignComponent.builder()
          .imageUrl(faker.internet().image())
          .user(userDataGenerator.generateTestUser(faker.internet().emailAddress()))
          .isPublic(faker.bool().bool())
          .build());
    }
    return designComponentRepository.saveAll(designComponents);
  }

  private List<ThemeStyle> generateTransientThemeStyles(ThemeComponent themeComponent,
      List<ColorStyle> colorStyles) {
    List<ThemeStyle> themeStyles = new ArrayList<>();
    for (ColorStyle colorStyle : colorStyles) {
      ThemeStyle themeStyle = ThemeStyle.builder()
          .themeComponent(themeComponent)
          .colorStyle(colorStyle)
          .color((faker.color().hex() + "FF"))
          .build();
      themeStyles.add(themeStyle);
      themeComponent.addThemeStyle(themeStyle);
    }
    return themeStyles;
  }

  private List<ThemeImage> generateTransientThemeImages(ThemeComponent themeComponent,
      List<ComponentType> componentTypes, List<DesignComponent> designComponents) {
    List<ThemeImage> themeImages = new ArrayList<>();
    for (ComponentType componentType : componentTypes) {
      DesignComponent designComponent = designComponents.get(
          faker.number().numberBetween(0, designComponents.size() - 1));
      ThemeImage themeImage = ThemeImage.builder()
          .componentType(componentType)
          .themeComponent(themeComponent)
          .designComponent(designComponent)
          .build();
      themeImages.add(themeImage);
      themeComponent.addThemeImage(themeImage);
    }
    return themeImages;
  }

  public List<ThemeComponent> generateThemeComponents(int amount, List<ColorStyle> colorStyles,
      List<ComponentType> componentTypes, List<DesignComponent> designComponents) {
    List<ThemeComponent> themeComponents = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      ThemeComponent themeComponent = ThemeComponent.builder()
          .themeName(faker.name().fullName())
          .userEmail(userEmail)
          .versionName(faker.name().fullName())
          .versionNumber(Integer.toString(faker.number().numberBetween(1, 100)))
          .isDone(i % 2 == 0)
          .isPublic(i % 2 == 0)
          .build();
      generateTransientThemeImages(themeComponent, componentTypes,
          designComponents);
      generateTransientThemeStyles(themeComponent, colorStyles);
      themeComponents.add(themeComponent);
    }
    return themeComponentRepository.saveAll(themeComponents);
  }
}
