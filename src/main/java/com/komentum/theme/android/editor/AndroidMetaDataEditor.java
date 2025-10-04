package com.komentum.theme.android.editor;

import com.komentum.theme.utils.ThemePathManager;
import com.komentum.theme.utils.XmlEditor;
import com.komentum.theme.utils.YamlEditor;
import com.komentum.theme.utils.YamlEditor.YamlData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidMetaDataEditor {

  private final XmlEditor xmlEditor;
  private final YamlEditor yamlEditor;

  // common
  private final static String THEME_PACKAGE_PATH_DIVIDER = ".";
  private final static String THEME_FILE_SYSTEM_DIVIDER = "/";
  private static final String[] THEME_BASE_PACKAGE_PATH = {"com", "kakao", "talk", "theme"};

  // apktool constants
  private final static String APKTOOL_FILE_NAME = "apktool.yml";
  private final static String APKTOOL_VERSION_INFO = "versionInfo";
  private final static String APKTOOL_VERSION_CODE = "versionCode";
  private final static String APKTOOL_VERSION_NAME = "versionName";
  private final static String APKTOOL_PACKAGE_INFO = "packageInfo";
  private final static String APKTOOL_RENAME_MANIFEST_PACKAGE = "renameManifestPackage";

  // AndroidManifest constants
  private final static String MANIFEST_FILE_NAME = "AndroidManifest.xml";
  private final static String MANIFEST_TAG_NAME = "manifest";
  private final static String MANIFEST_ACTIVITY_TAG_NAME = "activity";
  private final static String MANIFEST_PACKAGE_PROP = "package";
  private final static String MANIFEST_ANDROID_NAME_PROP = "android:name";
  private final static String MANIFEST_ACTIVITY_VALUE_SUFFIX = ".MainActivity";

  // apk smali constants
  private final static String SMALI_PACKAGE_NAME = "smali";
  private final static String SMALI_EXTENSION = ".smali";

  // strings.xml constants
  private final static String STRING_FILE_NAME = "strings.xml";
  private final static String[] STRING_FILE_DIRS = {"values", "values-ko", "values-ja"};
  private final static String STRING_TAG_NAME = "string";
  private final static String STRING_PROP_NAME = "name";
  private final static String STRING_NAME_PROP_VALUE = "app_name";
  private final static String STRING_THEME_TITLE_VALUE = "theme_title";


  private String generateThemePackagePath(String packageName, String divider) {
    StringJoiner joiner = new StringJoiner(divider);
    for (String basePackagePath : THEME_BASE_PACKAGE_PATH) {
      joiner.add(basePackagePath);
    }
    joiner.add(packageName);
    return joiner.toString();
  }

  private void editVersionInfo(YamlData apkToolYaml, String versionCode, String versionName)
      throws IOException {
    Map<String, Object> versionInfo = yamlEditor.getYamlValueMap(apkToolYaml, APKTOOL_VERSION_INFO);
    versionInfo.put(APKTOOL_VERSION_CODE, versionCode);
    versionInfo.put(APKTOOL_VERSION_NAME, versionName);
  }

  private void editPackageInfo(YamlData apkToolYaml, String packageName) throws IOException {
    String packagePath = generateThemePackagePath(packageName, THEME_PACKAGE_PATH_DIVIDER);
    Map<String, Object> packageInfo = yamlEditor.getYamlValueMap(apkToolYaml, APKTOOL_PACKAGE_INFO);
    packageInfo.put(APKTOOL_RENAME_MANIFEST_PACKAGE, packagePath);
  }

  private void editAndroidManifest(String themeId, String packageName) {
    Path manifestFilePath = Paths.get(ThemePathManager.getThemeDepackedDir(themeId).toString(),
        MANIFEST_FILE_NAME);
    Document manifestFileDocument = xmlEditor.loadDocument(manifestFilePath.toString());
    String updatedPackagePath = generateThemePackagePath(packageName, THEME_PACKAGE_PATH_DIVIDER);
    String updatedActivityPath = updatedPackagePath + MANIFEST_ACTIVITY_VALUE_SUFFIX;
    xmlEditor.editAttributeByTagName(manifestFileDocument, MANIFEST_TAG_NAME, MANIFEST_PACKAGE_PROP,
        updatedPackagePath);
    xmlEditor.editAttributeByTagName(manifestFileDocument, MANIFEST_ACTIVITY_TAG_NAME,
        MANIFEST_ANDROID_NAME_PROP,
        updatedActivityPath);
    xmlEditor.transform(manifestFilePath.toString(), manifestFileDocument);
  }

  private void editSmaliPackage(String themeId, String packageName) throws IOException {
    Path smaliThemePath = Paths.get(
        ThemePathManager.getThemeDepackedDir(themeId).toString(),
        Stream.concat(Stream.of(SMALI_PACKAGE_NAME), Arrays.stream(THEME_BASE_PACKAGE_PATH))
            .toArray(String[]::new)
    );
    Path oldPackagePath;
    try (Stream<Path> paths = Files.list(smaliThemePath)) {
      oldPackagePath = paths.filter(Files::isDirectory).findFirst()
          .orElseThrow(() -> new RuntimeException("package dir not found"));
    }
    // edit text on smali
    String oldPackageName = oldPackagePath.getFileName().toString();
    try (Stream<Path> paths = Files.walk(oldPackagePath)) {
      paths.filter(f -> f.getFileName().toString().endsWith(SMALI_EXTENSION)).forEach(path -> {
        try {
          String oldPackageDot = generateThemePackagePath(oldPackageName,
              THEME_PACKAGE_PATH_DIVIDER);
          String newPackageDot = generateThemePackagePath(packageName, THEME_PACKAGE_PATH_DIVIDER);
          String oldPackageSlash = generateThemePackagePath(oldPackageName,
              THEME_FILE_SYSTEM_DIVIDER);
          String newPackageSlash = generateThemePackagePath(packageName, THEME_FILE_SYSTEM_DIVIDER);
          String content = Files.readString(path, StandardCharsets.UTF_8);
          content = content.replace(oldPackageDot, newPackageDot);
          content = content.replace(oldPackageSlash, newPackageSlash);
          Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
    // edit package name
    Path newPackagePath = oldPackagePath.resolveSibling(packageName);
    Files.move(oldPackagePath, newPackagePath);
  }

  private void editThemeName(String themeId, String themeName) throws IOException {
    Path path = Paths.get(ThemePathManager.getThemeResourcePath(themeId).toString());
    Arrays.stream(STRING_FILE_DIRS).forEach(folderName -> {
      Path nameSheetPath = Paths.get(path.toString(), folderName, STRING_FILE_NAME);
      Document document = xmlEditor.loadDocument(nameSheetPath.toString());
      xmlEditor.editTextByTagAttr(document, STRING_TAG_NAME, STRING_PROP_NAME,
          STRING_NAME_PROP_VALUE, themeName);
      xmlEditor.editTextByTagAttr(document, STRING_TAG_NAME, STRING_PROP_NAME,
          STRING_THEME_TITLE_VALUE, themeName);
      xmlEditor.transform(nameSheetPath.toString(), document);
    });
  }

  public void editMetaData(String themeId, String themeName, String versionCode,
      String versionName) {
    try {
      String packageName = "theme" + themeId;
      // edit apktool.yaml renamePackage and version
      Path apkToolYamlPath = Paths.get(ThemePathManager.getThemeDepackedDir(themeId).toString(),
          APKTOOL_FILE_NAME);
      YamlData yamlData = yamlEditor.loadYamlMap(apkToolYamlPath);
      editVersionInfo(yamlData, versionCode, versionName);
      editPackageInfo(yamlData, packageName);
      yamlEditor.writeYaml(apkToolYamlPath, yamlData);
      // edit package and directory
      editAndroidManifest(themeId, packageName);
      editSmaliPackage(themeId, packageName);
      // edit theme name XML file
      editThemeName(themeId, themeName);
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
