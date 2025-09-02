package com.komentum.test;

import com.github.javafaker.Faker;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import com.komentum.theme.theme.repository.ThemeStyleRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThemeDataGenerator {

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

  private final Faker faker = new Faker();
  public String userEmail = "test@test.com";
  public List<ThemeComponent> initialThemes = new ArrayList<>();
  public List<ColorStyle> initialColorStyles = new ArrayList<>();
  public List<ComponentType> initialComponentTypes = new ArrayList<>();
  public List<DesignComponent> initialDesignComponents = new ArrayList<>();

  public void generateTestData(int themeCount, int stylePerTheme, int imagePerTheme) {
    initialColorStyles = generateColorStyles(stylePerTheme);
    initialComponentTypes = generateComponentTypes(imagePerTheme);
    initialDesignComponents = generateDesignComponents(initialComponentTypes);
    initialThemes = generateThemeComponents(themeCount, initialColorStyles, initialComponentTypes,
        initialDesignComponents);
  }

  public void deleteTestData() {
    themeComponentRepository.deleteAll();
    themeImageRepository.deleteAll();
    themeStyleRepository.deleteAll();
    colorStyleRepository.deleteAll();
    designComponentRepository.deleteAll();
    componentTypeRepository.deleteAll();
  }

  public List<ColorStyle> generateColorStyles(int amount) {
    List<ColorStyle> colorStyles = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      colorStyles.add(ColorStyle.builder()
          .styleSheetPath("style/sheet/path")
          .platform(Platform.ANDROID)
          .styleElementName("color")
          .stylePropsName("HeaderColor")
          .explain("explain")
          .build());
    }
    return colorStyleRepository.saveAll(colorStyles);
  }

  public List<ComponentType> generateComponentTypes(int amount) {
    List<ComponentType> componentTypes = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      componentTypes.add(ComponentType.builder()
          .androidComponentPath("android/path")
          .androidComponentName("filename.png")
          .iosComponentPath("ios/path")
          .iosComponentName("fileName.png")
          .sizeX(faker.number().numberBetween(1000, 2000))
          .sizeY(faker.number().numberBetween(3000, 4000))
          .explain("explain")
          .build());
    }
    return componentTypeRepository.saveAll(componentTypes);
  }

  public List<DesignComponent> generateDesignComponents(List<ComponentType> componentTypes) {
    List<DesignComponent> designComponents = new ArrayList<>();
    for (ComponentType componentType : componentTypes) {
      designComponents.add(DesignComponent.builder()
          .componentType(componentType)
          .imageUrl(faker.internet().image())
          .userEmail(faker.internet().emailAddress())
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
          .color(faker.color().hex())
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
          .isDone(false)
          .isPublic(false)
          .build();
      generateTransientThemeImages(themeComponent, componentTypes,
          designComponents);
      generateTransientThemeStyles(themeComponent, colorStyles);
      themeComponents.add(themeComponent);
    }
    return themeComponentRepository.saveAll(themeComponents);
  }
}
