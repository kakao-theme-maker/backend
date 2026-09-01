package com.komentum.theme.ios.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.theme.core.domain.ImageInset;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
    assertThat(result).contains("background-color: #112233FF;");
  }

  @Test
  void editCss_replacesMappedEightDigitColorWithoutHash() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
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
        .color("112233FF")
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
    assertThat(result).contains("background-color: #112233FF;");
  }

  @Test
  void editCss_appliesStoredAlphaAsRgbaSuffix() throws Exception {
    // given
    String css = """
        ManifestStyle
        {
            -kakaotalk-theme-name: 'Apeach';
            -kakaotalk-theme-version: '25.8.0';
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
        .color("#112233")
        .alpha(50)
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

    // then : alpha 50% -> round(50 * 255 / 100) = 128 = 0x80, RGBA 순서로 색상 뒤에 붙는다
    String result = Files.readString(tempDir.resolve("KakaoTalkTheme.css"), StandardCharsets.UTF_8);
    assertThat(result).contains("background-color: #11223380;");
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
    assertThat(result).contains("-ios-text-color: #123456FF;");
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

  @Test
  @DisplayName("css 내 bubble inset 정보를 수정한다")
  void editBubbleInset_success() throws Exception {
    // given
    String css = """
        MessageCellStyle-Send
        {
            -ios-background-image: 'temp.png' 10px 10px;
            -ios-selected-background-image: 'temp.png' 10px 10px;
            -ios-group-background-image: 'temp.png' 10px 10px;
            -ios-group-selected-background-image: 'temp.png' 10px 10px;
            -ios-title-edgeinsets: 10px 10px 10px 10px;  /* top, left, bottom, right */
            -ios-group-title-edgeinsets: 10px 10px 10px 10px;
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeImage sendBubble = ThemeImage.builder()
        .imageInset(new ImageInset(50, 50, 50, 50, 50, 50))
        .componentType(ComponentType.builder()
            .typeCode(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE)
            .build())
        .build();
    ThemeImage sendBubbleGroup = ThemeImage.builder()
        .imageInset(new ImageInset(50, 50, 50, 50, 50, 50))
        .componentType(ComponentType.builder()
            .typeCode(TypeCode.MESSAGE_CELL_STYLE_SEND_GROUP_BACKGROUND_IMAGE)
            .build())
        .build();
    // when
    editor.editBubbleInset(tempDir, List.of(sendBubble, sendBubbleGroup));
    // then
    String resultCss = Files.readString(tempDir.resolve("KakaoTalkTheme.css"),
        StandardCharsets.UTF_8);
    assertThat(resultCss).contains("-ios-background-image: 'temp.png' 50px 50px;");
    assertThat(resultCss).contains("-ios-selected-background-image: 'temp.png' 50px 50px;");
    assertThat(resultCss).contains("-ios-group-background-image: 'temp.png' 50px 50px;");
    assertThat(resultCss).contains("-ios-group-selected-background-image: 'temp.png' 50px 50px;");
    assertThat(resultCss).contains("-ios-title-edgeinsets: 50px 50px 50px 50px;");
    assertThat(resultCss).contains("-ios-group-title-edgeinsets: 50px 50px 50px 50px;");
  }

  @Test
  @DisplayName("메서드의 파라미터 내에는 말풍선 이미지만 허용되고 inset 정보가 있어야한다")
  void editBubbleInset_whenImage() throws Exception {
    // given
    String css = """
        MessageCellStyle-Send
        {
            -ios-background-image: 'temp.png' 10px 10px;
            -ios-selected-background-image: 'temp.png' 10px 10px;
            -ios-group-background-image: 'temp.png' 10px 10px;
            -ios-group-selected-background-image: 'temp.png' 10px 10px;
            -ios-title-edgeinsets: 10px 10px 10px 10px;  /* top, left, bottom, right */
            -ios-group-title-edgeinsets: 10px 10px 10px 10px;
        }
        """;
    Files.writeString(tempDir.resolve("KakaoTalkTheme.css"), css, StandardCharsets.UTF_8);
    ThemeImage invalidBubbleImage = ThemeImage.builder()
        .componentType(ComponentType.builder()
            .typeCode(TypeCode.MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE)
            .build())
        .build();
    ThemeImage bubbleImageWithoutInset = ThemeImage.builder()
        .componentType(ComponentType.builder()
            .typeCode(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE)
            .build())
        .build();
    // when & then
    assertThatThrownBy(() -> editor.editBubbleInset(tempDir, List.of(invalidBubbleImage)))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> editor.editBubbleInset(tempDir, List.of(bubbleImageWithoutInset)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
