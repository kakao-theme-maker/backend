package com.komentum.theme.ios.editor;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.enums.TypeCodeGroup;
import com.komentum.global.utils.RegexValidator;
import com.komentum.theme.core.domain.ImageInset;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.ios.enums.IOSBubbleInsetMapping;
import com.komentum.theme.ios.utils.IosThemePathManager;
import com.komentum.theme.utils.ColorEditor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IosThemeCssEditor {

  // 메타데이터 속성 목록
  private static final String MANIFEST_STYLE = "ManifestStyle";
  private static final String THEME_NAME = "-kakaotalk-theme-name";
  private static final String THEME_VERSION = "-kakaotalk-theme-version";
  private static final String AUTHOR_NAME = "-kakaotalk-author-name";
  private static final String THEME_ID = "-kakaotalk-theme-id";

  // inset 속성 목록
  private static final String BUBBLE_TITLE_EDGE_INSET = "-ios-title-edgeinsets";
  private static final String BUBBLE_GROUP_TITLE_EDGE_INSET = "-ios-group-title-edgeinsets";

  /**
   * iOS 테마의 색상 정보와 메타데이터를 수정한다
   * @param workDir iOS 테마의 루트 디렉토리 경로
   * @param themeComponent 수정할 테마 Entity
   * @param themeStyles 대상 테마의 색상 정보 목록
   * @param platformColorStyles iOS 테마 내 수정 가능한 색상 종류
   * */
  public void editCss(
      Path workDir,
      ThemeComponent themeComponent,
      List<ThemeStyle> themeStyles,
      List<PlatformColorStyle> platformColorStyles
  ) throws IOException {
    if (platformColorStyles == null || platformColorStyles.isEmpty()) {
      throw new IllegalStateException("iOS platform color style seed is required");
    }
    Path cssPath = IosThemePathManager.getCssPath(workDir);
    String css = Files.readString(cssPath, StandardCharsets.UTF_8);
    css = editManifest(css, themeComponent);
    css = editStyles(css, themeStyles, platformColorStyles);
    Files.writeString(cssPath, css, StandardCharsets.UTF_8);
  }

  /**
   * iOS 테마 내 말풍선들의 inset을 수정한다
   * @param workDir iOS 테마 루트 디렉토리
   * @param bubbles 말풍선 이미지 목록
   * @throws IllegalArgumentException 말풍선 이미지가 아니거나 말풍선인데 inset 정보가 없는 경우
   * */
  public void editBubbleInset(Path workDir, List<ThemeImage> bubbles) throws IOException {
    Path cssPath = IosThemePathManager.getCssPath(workDir);
    String css = Files.readString(cssPath, StandardCharsets.UTF_8);
    for (ThemeImage bubble : bubbles) {
      TypeCode typeCode = bubble.getComponentType().getTypeCode();
      // 각 이미지가 유효한 말풍선인지 확인
      if (!typeCode.getTypeCodeGroup().equals(TypeCodeGroup.CHATROOM_BUBBLE)) {
        throw new IllegalArgumentException(
            "[IosThemeCssEditor] themeImage is not bubble : " + typeCode);
      }
      if (bubble.getImageInset() == null || !bubble.getImageInset().isValid()) {
        throw new IllegalArgumentException(
            "[IosThemeCssEditor] invalid image bubble inset");
      }
      // 말풍선 inset 정보 수정
      IOSBubbleInsetMapping mapping = IOSBubbleInsetMapping.from(typeCode);
      for (String property : mapping.getProperties()) {
        // property가 edge inset인 경우
        if (property.equals(BUBBLE_TITLE_EDGE_INSET) ||
            property.equals(BUBBLE_GROUP_TITLE_EDGE_INSET)
        ) {
          css = editBubbleEdgeInset(bubble.getImageInset(), css, mapping.getSelector(), property);
        }
        // property가 bubble stretch인 경우
        else {
          css = editBubbleStretch(bubble.getImageInset(), css, mapping.getSelector(), property);
        }
      }
    }
    Files.writeString(cssPath, css, StandardCharsets.UTF_8);
  }

  /**
   * 말풍선 이미지의 stretch 영역(말풍선 내 늘어날 수 있는 영역) 정보를 수정한다
   * */
  private String editBubbleStretch(ImageInset imageInset, String css, String selector,
      String property) {
    // get css value
    String value = extractCssPropertyValue(css, selector, property);
    if (value == null || value.split(" ").length != 3) {
      log.warn("[IosThemeCssEditor] failed to apply bubble stretch : {}.{}", value, property);
      throw new IllegalArgumentException(
          "[IosThemeCssEditor] failed to apply bubble stretch : " + value + "." + property);
    }
    String[] target = value.split(" ");
    target[1] = imageInset.getStretchX() + "px";
    target[2] = imageInset.getStretchY() + "px";
    value = String.join(" ", target);
    return replaceCssProperty(css, selector, property, value);
  }

  /**
   * 말풍선 이미지의 inset 영역 ( 말풍선 내 padding ) 정보를 수정한다
   * */
  private String editBubbleEdgeInset(ImageInset imageInset, String css, String selector,
      String property) {
    String value = String.format("%dpx %dpx %dpx %dpx",
        imageInset.getEdgeInsetTop(),
        imageInset.getEdgeInsetLeft(),
        imageInset.getEdgeInsetBottom(),
        imageInset.getEdgeInsetRight());
    return replaceCssProperty(css, selector, property, value);
  }

  /**
   * iOS 테마의 메타데이터(이름, 버전, 작가, 테마 식별자)를 수정한다
   * @param css 테마의 css 파일 내용
   * @param themeComponent 수정할 대상 테마
   * */
  private String editManifest(String css, ThemeComponent themeComponent) {
    String result = css;
    result = replaceCssProperty(result, MANIFEST_STYLE, THEME_NAME,
        quote(themeComponent.getThemeName()));
    result = replaceCssProperty(result, MANIFEST_STYLE, THEME_VERSION,
        quote(resolveThemeVersion(themeComponent)));
    result = replaceCssProperty(result, MANIFEST_STYLE, AUTHOR_NAME,
        quote(themeComponent.getUserEmail()));
    result = replaceCssProperty(result, MANIFEST_STYLE, THEME_ID,
        quote("com.komentum.theme.ios.t" + themeComponent.getThemeComponentId()));
    return result;
  }

  /**
   * iOS 테마의 색상 정보를 수정한다
   * @param css 테마의 css 파일 내용
   * @param themeStyles 테마 내 색상 정보 목록
   * @param platformColorStyles iOS 테마 내 수정 가능한 색상 종류
   * */
  private String editStyles(
      String css,
      List<ThemeStyle> themeStyles,
      List<PlatformColorStyle> platformColorStyles
  ) {
    Map<Integer, ThemeStyle> themeStyleMap = themeStyles.stream()
        .collect(Collectors.toMap(
            themeStyle -> themeStyle.getColorStyle().getColorStyleId(),
            themeStyle -> themeStyle,
            (left, right) -> left
        ));
    String result = css;
    // iOS 시드의 resourceGroup과 resourceName을 각각 CSS 선택자와 속성으로 사용한다.
    // 대응하는 테마 색상이 없거나 유효하지 않으면 템플릿 기본 색상을 유지한다.
    for (PlatformColorStyle platformColorStyle : platformColorStyles) {
      ThemeStyle themeStyle = themeStyleMap.get(
          platformColorStyle.getColorStyle().getColorStyleId());
      if (themeStyle == null) {
        continue;
      }
      String color = normalizeCssColor(themeStyle.getColor(), platformColorStyle.getWeight());
      if (color == null) {
        continue;
      }
      result = replaceCssProperty(
          result,
          platformColorStyle.getResourceGroup(),
          platformColorStyle.getResourceName(),
          color
      );
    }
    return result;
  }

  /**
   * 특정 css 속성을 수정한다
   * @param css 테마 CSS 파일의 내용
   * @param selector CSS 파일에서 수정할 선택자
   * @param property 수정할 CSS 속성명
   * @param value CSS 속성에 적용할 값
   * @return 변경된 css 전체 파일
   * */
  private String replaceCssProperty(String css, String selector, String property, String value) {
    // 전달받은 선택자 블록 안에서 지정한 속성의 값만 정규식으로 교체한다.
    // 치환 그룹으로 속성 앞부분과 세미콜론을 보존하며,
    // 선택자와 여는 중괄호 사이의 블록 주석도 허용한다.
    Pattern pattern = Pattern.compile(
        "(" + Pattern.quote(selector) + "(?:\\s|/\\*.*?\\*/)*\\{[^}]*?"
            + Pattern.quote(property)
            + "\\s*:\\s*)[^;]*(;)",
        Pattern.DOTALL
    );
    Matcher matcher = pattern.matcher(css);
    if (!matcher.find()) {
      return css;
    }
    return matcher.replaceAll("$1" + Matcher.quoteReplacement(value) + "$2");
  }

  /**
   * css selector와 property로 value를 추출한다
   * @param css css 파일 내용
   * @param selector css selector
   * @param property css property
   * @return css property에 대한 value값
   * */
  private String extractCssPropertyValue(String css, String selector, String property) {
    Pattern pattern = Pattern.compile(
        "(" + Pattern.quote(selector) + "(?:\\s|/\\*.*?\\*/)*\\{[^}]*?"
            + Pattern.quote(property)
            + "\\s*:\\s*)([^;]*)(;)",
        Pattern.DOTALL
    );
    Matcher matcher = pattern.matcher(css);
    if (!matcher.find()) {
      return null;
    }
    return matcher.group(2).trim();
  }

  /**
   * CSS 속성값 내 작은따옴표를 제거한 후 작은따옴표로 감싼다. ( css injection 공격 방지 )
   * @param value 작은 따옴표로 묶을 값
   * @return 후처리된 속성값
   * */
  private String quote(String value) {
    // 매니페스트 값은 작은따옴표로 감싸므로 입력값의 작은따옴표를 제거해
    // CSS 문자열 선언이 중간에서 끊어지지 않도록 한다.
    return "'" + (value == null ? "" : value.replace("'", "")) + "'";
  }

  /**
   * 테마의 버전명을 반환하되, 버전명이 없다면 버전 번호를 반환한다
   * @param themeComponent 대상 테마 Entity
   * @return css에 삽입할 테마 버전명
   * */
  private String resolveThemeVersion(ThemeComponent themeComponent) {
    if (themeComponent.getVersionName() != null && !themeComponent.getVersionName().isBlank()) {
      return themeComponent.getVersionName();
    }
    return themeComponent.getVersionNumber();
  }

  private String normalizeCssColor(String color, double weight) {
    if (!RegexValidator.isValidHexColor(color)) {
      return null;
    }
    String normalized = color.trim();
    // 테마 내에서 6자리 hex color만 사용할 수 있음
    // 6자리 색상이고 #이 없다면, 6자리 hex color로 변환
    if (normalized.matches("[0-9a-fA-F]{6}")) {
      normalized = "#" + normalized;
    }
    // 8자리 색상이고 #이 없으면, 6자리 hex color로 변환
    else if (normalized.matches("[0-9a-fA-F]{8}")) {
      normalized = "#" + normalized.substring(0, 6);
    }
    // 8자리 색상이면, 6자리 hex color로 변환
    else if (normalized.matches("#[0-9a-fA-F]{8}")) {
      normalized = normalized.substring(0, 7);
    }
    return ColorEditor.toDarkColor(normalized, weight);
  }
}
