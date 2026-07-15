package com.komentum.theme.ios.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeStyle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IosThemeCssEditorTest {

  private final IosThemeCssEditor editor = new IosThemeCssEditor();

  @TempDir
  Path tempDir;

  @Test
  void editCss_replacesManifestAndMappedColors() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
            -kakaotalk-theme-url: 'http://www.kakao.com';
            -kakaotalk-author-name: 'Kakao Corp.';
            -kakaotalk-theme-id: 'com.kakao.talk.theme.apeachios';
        }
        MainViewStyle-Primary
        {
            background-color: #FFDEDE;
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(7)
        .themeName("My Theme")
        .userEmail("owner@test.com")
        .versionName("1.2.3")
        .versionNumber("12")
        .build();
    ColorStyle colorStyle = ColorStyle.builder()
        .colorStyleId(1)
        .styleCode(StyleCode.MAINVIEW_STYLE_BACKGROUND_COLOR)
        .name("background")
        .build();
    ThemeStyle themeStyle = ThemeStyle.builder()
        .colorStyle(colorStyle)
        .color("#112233FF")
        .build();
    PlatformColorStyle platformColorStyle = PlatformColorStyle.builder()
        .platform(Platform.IOS)
        .colorStyle(colorStyle)
        .resourceGroup("MainViewStyle-Primary")
        .resourceName("background-color")
        .code("test")
        .build();

    // when
    editor.editCss(tempDir, themeComponent, List.of(themeStyle), List.of(platformColorStyle));

    // then
    String result = Files.readString(tempDir.resolve("KakaoTalkTheme.css"), StandardCharsets.UTF_8);
    assertThat(result).contains("-kakaotalk-theme-name: 'My Theme';");
    assertThat(result).contains("-kakaotalk-theme-version: '1.2.3';");
    assertThat(result).contains("-kakaotalk-author-name: 'owner@test.com';");
    assertThat(result).contains("-kakaotalk-theme-id: 'com.komentum.theme.ios.t7';");
    assertThat(result).contains("background-color: #112233;");
  }

  @Test
  void editCss_replacesMappedColorWhenSelectorHasCommentBeforeBrace() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
            -kakaotalk-author-name: 'Kakao Corp.';
            -kakaotalk-theme-id: 'com.kakao.talk.theme.apeachios';
        }
        FeatureStyle-Primary                                       /* Primary : 버튼 텍스트 */
        {
            -ios-text-color: #805959;
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(7)
        .themeName("My Theme")
        .userEmail("owner@test.com")
        .versionName("1.2.3")
        .versionNumber("12")
        .build();
    ColorStyle colorStyle = ColorStyle.builder()
        .colorStyleId(1)
        .styleCode(StyleCode.FEATURE_STYLE_TEXT_COLOR)
        .name("feature")
        .build();
    ThemeStyle themeStyle = ThemeStyle.builder()
        .colorStyle(colorStyle)
        .color("#123456")
        .build();
    PlatformColorStyle platformColorStyle = PlatformColorStyle.builder()
        .platform(Platform.IOS)
        .colorStyle(colorStyle)
        .resourceGroup("FeatureStyle-Primary")
        .resourceName("-ios-text-color")
        .code("test")
        .build();

    // when
    editor.editCss(tempDir, themeComponent, List.of(themeStyle), List.of(platformColorStyle));

    // then
    String result = Files.readString(tempDir.resolve("KakaoTalkTheme.css"), StandardCharsets.UTF_8);
    assertThat(result).contains("-ios-text-color: #123456;");
  }

  @Test
  void editCss_keepsSampleValueWhenMappedColorIsBlank() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
            -kakaotalk-theme-url: 'http://www.kakao.com';
            -kakaotalk-author-name: 'Kakao Corp.';
            -kakaotalk-theme-id: 'com.kakao.talk.theme.apeachios';
        }
        TabBarStyle-Main
        {
            background-color: #FFFFFF;
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(7)
        .themeName("My Theme")
        .userEmail("owner@test.com")
        .versionName("1.2.3")
        .versionNumber("12")
        .build();
    ColorStyle colorStyle = ColorStyle.builder()
        .colorStyleId(1)
        .styleCode(StyleCode.TABBAR_STYLE_BACKGROUND_COLOR)
        .name("tab background")
        .build();
    ThemeStyle themeStyle = ThemeStyle.builder()
        .colorStyle(colorStyle)
        .color(" ")
        .build();
    PlatformColorStyle platformColorStyle = PlatformColorStyle.builder()
        .platform(Platform.IOS)
        .colorStyle(colorStyle)
        .resourceGroup("TabBarStyle-Main")
        .resourceName("background-color")
        .code("test")
        .build();

    // when
    editor.editCss(tempDir, themeComponent, List.of(themeStyle), List.of(platformColorStyle));

    // then
    String result = Files.readString(tempDir.resolve("KakaoTalkTheme.css"), StandardCharsets.UTF_8);
    assertThat(result).contains("background-color: #FFFFFF;");
    assertThat(result).doesNotContain("background-color: ;");
  }

  @Test
  void editCss_keepsSampleValueWhenMappedColorIsNotHex() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
            -kakaotalk-theme-url: 'http://www.kakao.com';
            -kakaotalk-author-name: 'Kakao Corp.';
            -kakaotalk-theme-id: 'com.kakao.talk.theme.apeachios';
        }
        TabBarStyle-Main
        {
            background-color: #FFFFFF;
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(7)
        .themeName("My Theme")
        .userEmail("owner@test.com")
        .versionName("1.2.3")
        .versionNumber("12")
        .build();
    ColorStyle colorStyle = ColorStyle.builder()
        .colorStyleId(1)
        .styleCode(StyleCode.TABBAR_STYLE_BACKGROUND_COLOR)
        .name("tab background")
        .build();
    ThemeStyle themeStyle = ThemeStyle.builder()
        .colorStyle(colorStyle)
        .color("plum")
        .build();
    PlatformColorStyle platformColorStyle = PlatformColorStyle.builder()
        .platform(Platform.IOS)
        .colorStyle(colorStyle)
        .resourceGroup("TabBarStyle-Main")
        .resourceName("background-color")
        .code("test")
        .build();

    // when
    editor.editCss(tempDir, themeComponent, List.of(themeStyle), List.of(platformColorStyle));

    // then
    String result = Files.readString(tempDir.resolve("KakaoTalkTheme.css"), StandardCharsets.UTF_8);
    assertThat(result).contains("background-color: #FFFFFF;");
    assertThat(result).doesNotContain("#plum");
  }

  @Test
  void editCss_throwsWhenIosPlatformColorMappingIsMissing() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
            -kakaotalk-theme-url: 'http://www.kakao.com';
            -kakaotalk-author-name: 'Kakao Corp.';
            -kakaotalk-theme-id: 'com.kakao.talk.theme.apeachios';
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(7)
        .themeName("My Theme")
        .userEmail("owner@test.com")
        .versionName("1.2.3")
        .versionNumber("12")
        .build();

    // when & then
    assertThatThrownBy(() -> editor.editCss(tempDir, themeComponent, List.of(), List.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("iOS platform color style seed is required");
  }
}
