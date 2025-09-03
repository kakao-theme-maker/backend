package com.komentum.theme.component.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentTypeTest {

    @Test
    void builder_ShouldCreateComponentTypeWithAllFields() {
        // When
        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("테스트 컴포넌트")
                .iosComponentPath("/ios/test")
                .iosComponentName("TestComponent")
                .androidComponentPath("/android/test")
                .androidComponentName("AndroidTest")
                .sizeX(100)
                .sizeY(50)
                .build();

        // Then
        assertThat(componentType.getComponentTypeId()).isEqualTo(1);
        assertThat(componentType.getExplain()).isEqualTo("테스트 컴포넌트");
        assertThat(componentType.getIosComponentPath()).isEqualTo("/ios/test");
        assertThat(componentType.getIosComponentName()).isEqualTo("TestComponent");
        assertThat(componentType.getAndroidComponentPath()).isEqualTo("/android/test");
        assertThat(componentType.getAndroidComponentName()).isEqualTo("AndroidTest");
        assertThat(componentType.getSizeX()).isEqualTo(100);
        assertThat(componentType.getSizeY()).isEqualTo(50);
    }

    @Test
    void builder_WithNullValues_ShouldCreateComponentType() {
        // When
        ComponentType componentType = ComponentType.builder()
                .explain("최소 컴포넌트")
                .build();

        // Then
        assertThat(componentType.getExplain()).isEqualTo("최소 컴포넌트");
        assertThat(componentType.getComponentTypeId()).isNull();
        assertThat(componentType.getIosComponentPath()).isNull();
        assertThat(componentType.getSizeX()).isNull();
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyComponentType() {
        // When
        ComponentType componentType = new ComponentType();

        // Then
        assertThat(componentType.getComponentTypeId()).isNull();
        assertThat(componentType.getExplain()).isNull();
        assertThat(componentType.getIosComponentPath()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldCreateComponentTypeWithAllFields() {
        // When
        ComponentType componentType = new ComponentType(1, "전체 생성자 테스트", "/ios/full", "FullComponent", "/android/full", "AndroidFull", 200, 100, new ArrayList<>(), null, null);

        // Then
        assertThat(componentType.getComponentTypeId()).isEqualTo(1);
        assertThat(componentType.getExplain()).isEqualTo("전체 생성자 테스트");
        assertThat(componentType.getSizeX()).isEqualTo(200);
        assertThat(componentType.getSizeY()).isEqualTo(100);
    }

    @Test
    void setters_ShouldModifyFields() {
        // Given
        ComponentType componentType = new ComponentType();

        // When
        componentType.setComponentTypeId(10);
        componentType.setExplain("수정된 설명");
        componentType.setSizeX(150);

        // Then
        assertThat(componentType.getComponentTypeId()).isEqualTo(10);
        assertThat(componentType.getExplain()).isEqualTo("수정된 설명");
        assertThat(componentType.getSizeX()).isEqualTo(150);
    }

}