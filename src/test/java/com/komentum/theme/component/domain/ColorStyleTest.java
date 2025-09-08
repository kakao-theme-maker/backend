package com.komentum.theme.component.domain;

import com.komentum.theme.component.enums.Platform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorStyleTest {

    @Test
    void builder_ShouldCreateColorStyleWithAllFields() {
        // When
        ColorStyle colorStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("배경색 스타일")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".container")
                .stylePropsName("background-color")
                .build();

        // Then
        assertThat(colorStyle.getColorTypeId()).isEqualTo(1);
        assertThat(colorStyle.getExplain()).isEqualTo("배경색 스타일");
        assertThat(colorStyle.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(colorStyle.getStyleSheetPath()).isEqualTo("styles/ios.css");
        assertThat(colorStyle.getStyleElementName()).isEqualTo(".container");
        assertThat(colorStyle.getStylePropsName()).isEqualTo("background-color");
    }

    @Test
    void builder_WithNullValues_ShouldCreateColorStyle() {
        // When
        ColorStyle colorStyle = ColorStyle.builder()
                .explain("최소 스타일")
                .build();

        // Then
        assertThat(colorStyle.getExplain()).isEqualTo("최소 스타일");
        assertThat(colorStyle.getColorTypeId()).isNull();
        assertThat(colorStyle.getPlatform()).isNull();
        assertThat(colorStyle.getStyleSheetPath()).isNull();
        assertThat(colorStyle.getStyleElementName()).isNull();
        assertThat(colorStyle.getStylePropsName()).isNull();
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyColorStyle() {
        // When
        ColorStyle colorStyle = new ColorStyle();

        // Then
        assertThat(colorStyle.getColorTypeId()).isNull();
        assertThat(colorStyle.getExplain()).isNull();
        assertThat(colorStyle.getPlatform()).isNull();
        assertThat(colorStyle.getStyleSheetPath()).isNull();
        assertThat(colorStyle.getStyleElementName()).isNull();
        assertThat(colorStyle.getStylePropsName()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldCreateColorStyleWithAllFields() {
        // When
        ColorStyle colorStyle = new ColorStyle(
                1, "전체 생성자 테스트", Platform.IOS, "styles/ios.css", ".test", "color"
        );

        // Then
        assertThat(colorStyle.getColorTypeId()).isEqualTo(1);
        assertThat(colorStyle.getExplain()).isEqualTo("전체 생성자 테스트");
        assertThat(colorStyle.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(colorStyle.getStyleSheetPath()).isEqualTo("styles/ios.css");
        assertThat(colorStyle.getStyleElementName()).isEqualTo(".test");
        assertThat(colorStyle.getStylePropsName()).isEqualTo("color");
    }

    @Test
    void setters_ShouldModifyFields() {
        // Given
        ColorStyle colorStyle = new ColorStyle();

        // When
        colorStyle.setColorTypeId(5);
        colorStyle.setExplain("수정된 설명");
        colorStyle.setPlatform(Platform.IOS);
        colorStyle.setStyleSheetPath("styles/ios.css");
        colorStyle.setStyleElementName(".modified");
        colorStyle.setStylePropsName("color");

        // Then
        assertThat(colorStyle.getColorTypeId()).isEqualTo(5);
        assertThat(colorStyle.getExplain()).isEqualTo("수정된 설명");
        assertThat(colorStyle.getPlatform()).isEqualTo(Platform.IOS);
        assertThat(colorStyle.getStyleSheetPath()).isEqualTo("styles/ios.css");
        assertThat(colorStyle.getStyleElementName()).isEqualTo(".modified");
        assertThat(colorStyle.getStylePropsName()).isEqualTo("color");
    }


}