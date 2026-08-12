package com.komentum.theme.android.editor;

import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.android.utils.XmlEditor;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidMetaDataEditor {

  private final XmlEditor xmlEditor;

  // strings.xml constants
  private final static String THEME_METADATA_XML_NAME = "strings.xml";
  private final static String[] THEME_METADATA_DIRECTORIES = {"values", "values-ko", "values-ja"};
  private final static String THEME_METADATA_TAG_NAME = "string";
  private final static String THEME_METADATA_PROPS_KEY = "name";
  private final static String THEME_APP_NAME_PROPS_VALUE = "app_name";
  private final static String THEME_TITLE_PROPS_VALUE = "theme_title";

  public void editThemeName(String themeId, String themeName) {
    Path themeMetaDataPath = ThemePathManager.getAndroidResourcePath(themeId);
    for (String themeNameFileDirectory : THEME_METADATA_DIRECTORIES) {
      Path themeNameXmlPath = themeMetaDataPath.resolve(themeNameFileDirectory)
          .resolve(THEME_METADATA_XML_NAME);
      Document document = xmlEditor.loadDocument(themeNameXmlPath.toString());
      xmlEditor.editTextByTagAttr(document, THEME_METADATA_TAG_NAME, THEME_METADATA_PROPS_KEY,
          THEME_APP_NAME_PROPS_VALUE, themeName);
      xmlEditor.editTextByTagAttr(document, THEME_METADATA_TAG_NAME, THEME_METADATA_PROPS_KEY,
          THEME_TITLE_PROPS_VALUE, themeName);
      xmlEditor.transform(themeNameXmlPath.toString(), document);
    }
  }
}
