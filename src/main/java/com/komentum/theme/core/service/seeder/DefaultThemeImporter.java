package com.komentum.theme.core.service.seeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.utils.JsonUtils;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.theme.core.service.ThemeStyleService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultThemeImporter {

  private final JsonUtils jsonUtils;
  private final ThemeImageService themeImageService;
  private final ThemeStyleService themeStyleService;
  private final ThemeComponentRepository themeComponentRepository;
  private final UserEntityFinder userEntityFinder;

  private static final String THEME_CODE_JSON_PATH = "theme-data/defaultThemes/themeCode.json";
  private static final String DEFAULT_THEME_INFO_LIST_KEY = "defaultThemeInfoList";
  private static final String THEME_NAME_KEY = "themeName";
  private static final String THEME_CODE_KEY = "themeCode";

  @Getter
  @Builder
  @AllArgsConstructor
  private static class DefaultThemeInfo {

    private final String themeName;
    private final String themeCode;
  }

  /**
   * json 데이터로부터 디폴트 테마 정보들을 조회한다
   * */
  private List<DefaultThemeInfo> readDefaultThemeCodes() {
    try {
      JsonNode root = jsonUtils.readJsonNode(THEME_CODE_JSON_PATH);
      List<DefaultThemeInfo> defaultThemeInfoList = new ArrayList<>();
      for (JsonNode node : root.get(DEFAULT_THEME_INFO_LIST_KEY)) {
        defaultThemeInfoList.add(DefaultThemeInfo.builder()
            .themeName(node.get(THEME_NAME_KEY).asText())
            .themeCode(node.get(THEME_CODE_KEY).asText())
            .build());
      }
      return defaultThemeInfoList;
    } catch (IOException e) {
      log.error(e.getMessage());
      throw new RuntimeException("[DefaultThemeImporter] failed to load defaultThemeCodes", e);
    }
  }

  /**
   * todo: 현재 개발 불가. 112번 PR 머지 후 진행 가능
   * */
  @Transactional
  public void importDefaultTheme(String publicUserId) {
    User rootUser = userEntityFinder.findUserEntity(publicUserId);
    TypeCode[] typeCodes = TypeCode.values();
    StyleCode[] styleCodes = StyleCode.values();
    List<DefaultThemeInfo> defaultThemeInfoList = readDefaultThemeCodes();
    for (DefaultThemeInfo defaultThemeInfo : defaultThemeInfoList) {
      // if the theme already exists, continue
      if (themeComponentRepository.existsByThemeCode(defaultThemeInfo.themeCode)) {
        continue;
      }
      // generate theme component
      ThemeComponent themeComponent = themeComponentRepository.save(
          ThemeComponent.builder()
              .themeName(defaultThemeInfo.themeName)
              .themeCode(defaultThemeInfo.themeCode)
              .userEmail(rootUser.getUserEmail())
              .versionName(defaultThemeInfo.themeCode + ".0.0.1")
              .versionNumber("0")
              .isPublic(true)
              .isDone(true)
              .build()
      );
      // generate theme images
      for (TypeCode typeCode : typeCodes) {
        // todo
      }
      for (StyleCode styleCode : styleCodes) {
        // todo
      }
    }
  }
}
