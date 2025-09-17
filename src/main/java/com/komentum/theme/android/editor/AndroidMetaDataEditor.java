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
import java.util.List;
import java.util.Map;
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

  private String generateThemePackagePath(String packageName, String divider) {
    return "com"
        + divider
        + "kakao"
        + divider
        + "talk"
        + divider
        + "theme"
        + divider
        + packageName;
  }

  private void editVersionInfo(YamlData apkToolYaml, String versionCode, String versionName)
      throws IOException {
    Map<String, Object> versionInfo = yamlEditor.getYamlValueMap(apkToolYaml, "versionInfo");
    versionInfo.put("versionCode", versionCode);
    versionInfo.put("versionName", versionName);
  }

  private void editPackageInfo(YamlData apkToolYaml, String packageName) throws IOException {
    String packagePath = generateThemePackagePath(packageName, ".");
    Map<String, Object> packageInfo = yamlEditor.getYamlValueMap(apkToolYaml, "packageInfo");
    packageInfo.put("renameManifestPackage", packagePath);
  }

  private void editAndroidManifest(String themeId, String packageName) {
    Path manifestFilePath = Paths.get(ThemePathManager.getThemeDepackedDir(themeId).toString(),
        "AndroidManifest.xml");
    Document manifestFileDocument = xmlEditor.loadDocument(manifestFilePath.toString());
    String updatedPackagePath = generateThemePackagePath(packageName, ".");
    String updatedActivityPath = updatedPackagePath + ".MainActivity";
    xmlEditor.editAttributeByTagName(manifestFileDocument, "manifest", "package",
        updatedPackagePath);
    xmlEditor.editAttributeByTagName(manifestFileDocument, "activity", "android:name",
        updatedActivityPath);
    xmlEditor.transform(manifestFilePath.toString(), manifestFileDocument);
  }

  private void editSmaliPackage(String themeId, String packageName) throws IOException {
    Path smaliThemePath = Paths.get(ThemePathManager.getThemeDepackedDir(themeId).toString(),
        "smali",
        "com", "kakao", "talk", "theme");
    Path oldPackagePath;
    try (Stream<Path> paths = Files.list(smaliThemePath)) {
      oldPackagePath = paths.filter(Files::isDirectory).findFirst()
          .orElseThrow(() -> new RuntimeException("package dir not found"));
    }
    // edit text on smali
    String oldPackageName = oldPackagePath.getFileName().toString();
    try (Stream<Path> paths = Files.walk(oldPackagePath)) {
      paths.filter(f -> f.getFileName().toString().endsWith(".smali")).forEach(path -> {
        try {
          String oldPackageDot = generateThemePackagePath(oldPackageName, ".");
          String newPackageDot = generateThemePackagePath(packageName, ".");
          String oldPackageSlash = generateThemePackagePath(oldPackageName, "/");
          String newPackageSlash = generateThemePackagePath(packageName, "/");
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
    List<String> valueDirs = List.of("values", "values-ko", "values-ja");
    valueDirs.forEach(v -> {
      Path nameSheetPath = Paths.get(path.toString(), v, "strings.xml");
      Document document = xmlEditor.loadDocument(nameSheetPath.toString());
      xmlEditor.editTextByTagAttr(document, "string", "name", "app_name", themeName);
      xmlEditor.editTextByTagAttr(document, "string", "name", "theme_title", themeName);
      xmlEditor.transform(nameSheetPath.toString(), document);
    });
  }

  public void editMetaData(String themeId, String themeName, String versionCode,
      String versionName) {
    try {
      String packageName = "theme" + themeId;
      // edit apktool.yaml renamePackage and version
      Path apkToolYamlPath = Paths.get(ThemePathManager.getThemeDepackedDir(themeId).toString(),
          "apktool.yml");
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
