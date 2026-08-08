package com.komentum.theme.core.service.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.designcomponent.service.ColorStyleService;
import com.komentum.designcomponent.service.ComponentTypeService;
import com.komentum.designcomponent.service.PlatformColorStyleService;
import com.komentum.designcomponent.service.PlatformComponentTypeService;
import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.JsonUtils;
import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.android.utils.XmlEditor;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.enums.ThemeType;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultThemeSeeder {

  private final JsonUtils jsonUtils;
  private final ThemeStyleRepository themeStyleRepository;
  private final ThemeImageRepository themeImageRepository;
  private final ThemeComponentRepository themeComponentRepository;
  private final DesignComponentRepository designComponentRepository;
  private final ComponentTypeService componentTypeService;
  private final ColorStyleService colorStyleService;
  private final UserEntityFinder userEntityFinder;
  private final FileManager fileManager;
  private final PlatformComponentTypeService platformComponentTypeService;
  private final PlatformColorStyleService platformColorStyleService;
  private final XmlEditor xmlEditor;

  private static final String THEME_CODE_JSON_PATH = "defaultThemes/themeCode.json";
  private static final String DEFAULT_THEME_INFO_LIST_KEY = "defaultThemeInfoList";
  private static final String THEME_NAME_KEY = "themeName";
  private static final String THEME_CODE_KEY = "themeCode";
  private static final String THEME_RESOURCE_PATH_KEY = "resourcePath";

  @Getter
  @Builder
  @AllArgsConstructor
  private static class DefaultThemeSheetInfo {

    private final String themeName;
    private final String themeCode;
    private final String resourcePath;
  }

  @Getter
  @Builder
  @AllArgsConstructor
  private static class ColorResourceInfo {

    private final String resourceGroup;
    private final String resourceName;
    private final String color;
  }

  @Getter
  @Builder
  private static class DefaultThemeContext {

    private final Map<TypeCode, ComponentType> componentTypeMap;
    private final Map<TypeCode, List<PlatformComponentType>> platformComponentTypeMap;

    private final Map<StyleCode, ColorStyle> colorStyleMap;
    private final Map<StyleCode, List<PlatformColorStyle>> platformColorStyleMap;
  }

  /**
   * 디폴트 테마 압축 파일로부터 테마 데이터를 생성한다
   * */
  @Transactional
  public void seedDefaultThemes(String publicUserId) {
    // 1. 필요한 데이터 조회 ( user, context )
    User rootUser = userEntityFinder.findUserEntity(publicUserId);
    DefaultThemeContext context = loadContext();
    // 2. themeCode.json으로부터 디폴트 테마 데이터 목록을 조회한다
    List<DefaultThemeSheetInfo> defaultThemeSheetInfoList = readDefaultThemeSheetInfo();
    for (DefaultThemeSheetInfo defaultThemeSheetInfo : defaultThemeSheetInfoList) {
      if (themeComponentRepository.existsByThemeCode(defaultThemeSheetInfo.themeCode)) {
        continue;
      }
      ThemeComponent themeComponent = themeComponentRepository.save(
          ThemeComponent.builder()
              .themeName(defaultThemeSheetInfo.themeName)
              .themeCode(defaultThemeSheetInfo.themeCode)
              .userEmail(rootUser.getUserEmail())
              .versionName(defaultThemeSheetInfo.themeCode + ".0.0.1")
              .versionNumber("0")
              .themeType(ThemeType.DEFAULT)
              .isPublic(true)
              .isDone(true)
              .build()
      );
      // 3. DefaultTheme를 압축해제한다.
      Path defaultThemeRootPath = ThemePathManager.getDefaultThemeDir(
          themeComponent.getThemeComponentId());
      try (InputStream is = new ClassPathResource(
          defaultThemeSheetInfo.getResourcePath()).getInputStream()
      ) {
        unzipFile(is, defaultThemeRootPath);
        // 4. themeImage, designComponent를 생성한다
        seedDefaultThemeImages(themeComponent, rootUser, defaultThemeRootPath, context);
        // 5. themeStyle을 생성한다.
        seedDefaultThemeStyles(themeComponent, defaultThemeRootPath, context);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } finally {
        try {
          FileSystemUtils.deleteRecursively(defaultThemeRootPath);
        } catch (IOException e) {
          log.warn("failed to delete default theme directory: {}", defaultThemeRootPath, e);
        }
      }
    }
  }

  /**
   * 1. 시드 생성에 필요한 데이터들을 조회한다
   * */
  private DefaultThemeContext loadContext() {
    return DefaultThemeContext.builder()
        .colorStyleMap(colorStyleService.findColorStyleMap())
        .componentTypeMap(componentTypeService.findComponentTypeMap())
        .platformColorStyleMap(
            platformColorStyleService.findStyleCodeMapByPlatform(Platform.ANDROID))
        .platformComponentTypeMap(
            platformComponentTypeService.findTypeCodeMapByPlatform(Platform.ANDROID))
        .build();
  }

  /**
   * 2. themeCode.json 데이터로부터 디폴트 테마 정보들을 조회한다
   * */
  private List<DefaultThemeSheetInfo> readDefaultThemeSheetInfo() {
    try {
      JsonNode root = jsonUtils.readJsonNode(THEME_CODE_JSON_PATH);
      List<DefaultThemeSheetInfo> defaultThemeSheetInfoList = new ArrayList<>();
      for (JsonNode node : root.get(DEFAULT_THEME_INFO_LIST_KEY)) {
        defaultThemeSheetInfoList.add(DefaultThemeSheetInfo.builder()
            .themeName(node.get(THEME_NAME_KEY).asText())
            .themeCode(node.get(THEME_CODE_KEY).asText())
            .resourcePath(node.get(THEME_RESOURCE_PATH_KEY).asText())
            .build());
      }
      return defaultThemeSheetInfoList;
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new UncheckedIOException("[DefaultThemeSeeder] failed to load defaultThemeCodes", e);
    }
  }

  /**
   * 3. 파일을 특정 경로에 압축 해제한다
   * TODO : 추후 다른 PR의 CompressUtils로 대체해야함
   * */
  private void unzipFile(InputStream inputStream, Path destination) throws IOException {
    destination = destination.toAbsolutePath().normalize();
    try (ZipInputStream zis = new ZipInputStream(inputStream)) {
      Files.createDirectories(destination);
      ZipEntry zipEntry;
      while ((zipEntry = zis.getNextEntry()) != null) {
        // 파일을 저장할 경로 단순화
        Path target = destination.resolve(zipEntry.getName()).normalize();
        // 파일을 저장할 경로의 유효성 검사
        if (!target.startsWith(destination)) {
          throw new IOException(
              "Invalid zip entry outside destination: " + zipEntry.getName()
          );
        }
        // 디렉토리면 생성, 파일이면 부모 디렉토리 생성 후 파일 복사
        if (zipEntry.isDirectory()) {
          Files.createDirectories(target);
        } else {
          Files.createDirectories(target.getParent());
          Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  /**
   * 4. themeImage, designComponent를 생성한다.
   * 하나의 themeImage는 하나의 typeCode와 매핑되고, typeCode 중복은 없다 ( 하나의 요소에 서로 다른 이미지 존재 불가 )
   * */
  private void seedDefaultThemeImages(ThemeComponent theme, User user, Path themeRootPath,
      DefaultThemeContext context) throws IOException {
    TypeCode[] typeCodes = TypeCode.values();
    List<ThemeImage> themeImages = new ArrayList<>();
    List<DesignComponent> designComponentsToSave = new ArrayList<>();
    for (TypeCode typeCode : typeCodes) {
      ComponentType componentType = context.componentTypeMap.get(typeCode);
      List<PlatformComponentType> platformComponentTypes = context.platformComponentTypeMap.get(
          typeCode);
      if (platformComponentTypes == null || componentType == null) {
        throw new EntityNotFoundException(
            "platformComponentType with typeCode" + typeCode.getTypeCode() + "not exists");
      }
      DesignComponent designComponent = createDefaultDesignComponent(user, themeRootPath,
          componentType, platformComponentTypes);
      designComponentsToSave.add(designComponent);
      themeImages.add(ThemeImage.builder()
          .themeComponent(theme)
          .designComponent(designComponent)
          .componentType(componentType)
          .build());
    }
    designComponentRepository.saveAll(designComponentsToSave);
    themeImageRepository.saveAll(themeImages);
  }

  /**
   * design component를 생성한다.
   * */
  private DesignComponent createDefaultDesignComponent(User user, Path themeRootPath,
      ComponentType componentType, List<PlatformComponentType> platformComponentTypes)
      throws IOException {
    PlatformComponentType pct = platformComponentTypes.get(0);
    Path imagePath = ThemePathManager.getAndroidThemeImagePath(themeRootPath, pct.getPath());
    String imageUrl = fileManager.uploadFile(Files.readAllBytes(imagePath),
        UUID.randomUUID().toString());
    DesignComponent dc = DesignComponent.builder()
        .imageUrl(imageUrl)
        .user(user)
        .isPublic(true)
        .build();
    dc.replaceComponentTypes(List.of(componentType));
    return dc;
  }

  /**
   * 5. ThemeStyle을 생성한다.
   * 하나의 themeStyle은 하나의 styleCode와 매핑되고, styleCode 중복은 없다 ( 하나의 스타일에 서로 다른 색상이 존재할 수 없음 )
   * */
  private void seedDefaultThemeStyles(ThemeComponent theme, Path themeRootPath,
      DefaultThemeContext context) {
    StyleCode[] styleCodes = StyleCode.values();
    Path colorSheetPath = ThemePathManager.getAndroidColorSheetPath(themeRootPath);
    // android는 resourceGroup이 color로 동일, resourceName은 고유값을 갖는다
    Map<String, ColorResourceInfo> colorResourceInfoMap = readColorSheet(colorSheetPath).stream()
        .collect(Collectors.toMap(
            ColorResourceInfo::getResourceName,
            Function.identity()
        ));
    List<ThemeStyle> themeStyles = new ArrayList<>();
    for (StyleCode styleCode : styleCodes) {
      ColorStyle colorStyle = context.colorStyleMap.get(styleCode);
      List<PlatformColorStyle> platformColorStyles = context.getPlatformColorStyleMap()
          .get(styleCode);
      if (colorStyle == null || platformColorStyles == null) {
        throw new EntityNotFoundException(
            "platformColorStyle with styleCode " + styleCode.getStyleCode() + " not exists");
      }
      PlatformColorStyle pcs = platformColorStyles.get(0);
      ColorResourceInfo colorResourceInfo = colorResourceInfoMap.get(pcs.getResourceName());
      if (colorResourceInfo == null) {
        throw new RuntimeException(
            "[DefaultThemeSeeder] color resource with resourceName " + pcs.getResourceName()
                + " not exists");
      }
      themeStyles.add(ThemeStyle.builder()
          .themeComponent(theme)
          .colorStyle(colorStyle)
          .color(colorResourceInfo.color)
          .build());
    }
    themeStyleRepository.saveAll(themeStyles);
  }

  private List<ColorResourceInfo> readColorSheet(Path colorSheetPath) {
    String resourceGroup = "color";
    Document document = xmlEditor.loadDocument(colorSheetPath.toAbsolutePath().toString());
    NodeList colors = document.getElementsByTagName(resourceGroup);
    List<ColorResourceInfo> colorResourceInfoList = new ArrayList<>();
    for (int i = 0; i < colors.getLength(); i++) {
      Element colorElement = (Element) colors.item(i);
      String colorCode = colorElement.getTextContent();
      String resourceName = colorElement.getAttribute("name");
      colorResourceInfoList.add(ColorResourceInfo.builder()
          .color(colorCode)
          .resourceGroup(resourceGroup)
          .resourceName(resourceName)
          .build());
    }
    return colorResourceInfoList;
  }
}
