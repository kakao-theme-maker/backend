package com.komentum.theme.component.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorStyleTest {

    @Test
    void builder_ShouldCreateColorStyleWithAllFields() {
        // When
        ColorStyle colorStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("배경색 스타일")
                .iosStyleName(".container|background-color")
                .androidStyleName("background_color")
                .build();

        // Then
        assertThat(colorStyle.getColorTypeId()).isEqualTo(1);
        assertThat(colorStyle.getExplain()).isEqualTo("배경색 스타일");
        assertThat(colorStyle.getIosStyleName()).isEqualTo(".container|background-color");
        assertThat(colorStyle.getAndroidStyleName()).isEqualTo("background_color");
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
        assertThat(colorStyle.getIosStyleName()).isNull();
        assertThat(colorStyle.getAndroidStyleName()).isNull();
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyColorStyle() {
        // When
        ColorStyle colorStyle = new ColorStyle();

        // Then
        assertThat(colorStyle.getColorTypeId()).isNull();
        assertThat(colorStyle.getExplain()).isNull();
        assertThat(colorStyle.getIosStyleName()).isNull();
        assertThat(colorStyle.getAndroidStyleName()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldCreateColorStyleWithAllFields() {
        // When
        ColorStyle colorStyle = new ColorStyle(
                1, "전체 생성자 테스트", ".test|color", "test_color"
        );

        // Then
        assertThat(colorStyle.getColorTypeId()).isEqualTo(1);
        assertThat(colorStyle.getExplain()).isEqualTo("전체 생성자 테스트");
        assertThat(colorStyle.getIosStyleName()).isEqualTo(".test|color");
        assertThat(colorStyle.getAndroidStyleName()).isEqualTo("test_color");
    }

    @Test
    void setters_ShouldModifyFields() {
        // Given
        ColorStyle colorStyle = new ColorStyle();

        // When
        colorStyle.setColorTypeId(5);
        colorStyle.setExplain("수정된 설명");
        colorStyle.setIosStyleName(".modified|color");
        colorStyle.setAndroidStyleName("modified_color");

        // Then
        assertThat(colorStyle.getColorTypeId()).isEqualTo(5);
        assertThat(colorStyle.getExplain()).isEqualTo("수정된 설명");
        assertThat(colorStyle.getIosStyleName()).isEqualTo(".modified|color");
        assertThat(colorStyle.getAndroidStyleName()).isEqualTo("modified_color");
    }


}