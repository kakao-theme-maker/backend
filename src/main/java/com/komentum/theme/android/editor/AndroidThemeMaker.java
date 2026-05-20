package com.komentum.theme.android.editor;

import com.komentum.theme.android.dto.AndroidColorDto;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.android.utils.ThemePathManager;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidThemeMaker {

  private final AndroidThemeInitializer androidThemeInitializer;
  private final AndroidThemeSaver androidThemeSaver;
  private final AndroidMetaDataEditor androidMetaDataEditor;
  private final AndroidColorStyleEditor androidColorStyleEditor;
  private final AndroidThemeImageEditor androidThemeImageEditor;
  private final ThemeComponentRepository themeComponentRepository;

  public void removeLocalThemeFiles(int themeId) {
    try {
      Path themePath = ThemePathManager.getThemeDir(Integer.toString(themeId));
      FileUtils.deleteDirectory(themePath.toFile());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  /**
   * make a theme with the specific theme id
   *
   * @param themeId theme id
   */
  @Transactional
  public String makeTheme(int themeId) {
    try {
      ThemeComponent themeComponent = themeComponentRepository.findById(themeId)
          .orElseThrow(() -> new RuntimeException("ThemeComponent is not found"));
      List<AndroidColorDto> colorDtoList = themeComponent.getThemeStyles().stream()
          .map(AndroidColorDto::fromEntity).toList();
      List<AndroidComponentDto> componentDtoList = themeComponent.getThemeImages().stream()
          .map(AndroidComponentDto::fromEntity).toList();
      androidThemeInitializer.initTheme(Integer.toString(themeId));
      androidThemeImageEditor.editImages(Integer.toString(themeId), componentDtoList);
      androidMetaDataEditor.editMetaData(Integer.toString(themeId), themeComponent.getThemeName(),
          themeComponent.getVersionNumber(), themeComponent.getVersionName());
      androidColorStyleEditor.editColors(Integer.toString(themeId), colorDtoList);
      return androidThemeSaver.repackAndSaveTheme(Integer.toString(themeId));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
}
