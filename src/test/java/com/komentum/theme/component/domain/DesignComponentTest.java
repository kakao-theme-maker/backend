package com.komentum.theme.component.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DesignComponentTest {

    @Test
    void builder_ShouldCreateDesignComponentWithAllFields() {
        // Given
        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼 컴포넌트")
                .build();

        LocalDateTime now = LocalDateTime.now();

        // When
        DesignComponent designComponent = DesignComponent.builder()
                .designComponentId(1)
                .userEmail("test@example.com")
                .componentType(componentType)
                .imageUrl("https://example.com/image.png")
                .isPublic(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(designComponent.getDesignComponentId()).isEqualTo(1);
        assertThat(designComponent.getUserEmail()).isEqualTo("test@example.com");
        assertThat(designComponent.getComponentType()).isEqualTo(componentType);
        assertThat(designComponent.getImageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(designComponent.getIsPublic()).isTrue();
        assertThat(designComponent.getCreatedAt()).isEqualTo(now);
        assertThat(designComponent.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void builder_WithMinimalFields_ShouldCreateDesignComponent() {
        // When
        DesignComponent designComponent = DesignComponent.builder()
                .userEmail("minimal@example.com")
                .imageUrl("https://example.com/minimal.png")
                .build();

        // Then
        assertThat(designComponent.getUserEmail()).isEqualTo("minimal@example.com");
        assertThat(designComponent.getImageUrl()).isEqualTo("https://example.com/minimal.png");
        assertThat(designComponent.getDesignComponentId()).isNull();
        assertThat(designComponent.getComponentType()).isNull();
        assertThat(designComponent.getIsPublic()).isNull();
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyDesignComponent() {
        // When
        DesignComponent designComponent = new DesignComponent();

        // Then
        assertThat(designComponent.getDesignComponentId()).isNull();
        assertThat(designComponent.getUserEmail()).isNull();
        assertThat(designComponent.getComponentType()).isNull();
        assertThat(designComponent.getImageUrl()).isNull();
        assertThat(designComponent.getIsPublic()).isNull();
        assertThat(designComponent.getCreatedAt()).isNull();
        assertThat(designComponent.getUpdatedAt()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldCreateDesignComponentWithAllFields() {
        // Given
        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("테스트 컴포넌트")
                .build();
        LocalDateTime now = LocalDateTime.now();

        // When
        DesignComponent designComponent = new DesignComponent(
                1, "test@example.com", componentType,
                "https://example.com/test.png", true, now, now
        );

        // Then
        assertThat(designComponent.getDesignComponentId()).isEqualTo(1);
        assertThat(designComponent.getUserEmail()).isEqualTo("test@example.com");
        assertThat(designComponent.getComponentType()).isEqualTo(componentType);
        assertThat(designComponent.getImageUrl()).isEqualTo("https://example.com/test.png");
        assertThat(designComponent.getIsPublic()).isTrue();
        assertThat(designComponent.getCreatedAt()).isEqualTo(now);
        assertThat(designComponent.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void setters_ShouldModifyFields() {
        // Given
        DesignComponent designComponent = new DesignComponent();
        ComponentType componentType = ComponentType.builder()
                .componentTypeId(2)
                .explain("수정된 컴포넌트")
                .build();

        // When
        designComponent.setDesignComponentId(10);
        designComponent.setUserEmail("modified@example.com");
        designComponent.setComponentType(componentType);
        designComponent.setImageUrl("https://example.com/modified.png");
        designComponent.setIsPublic(false);

        // Then
        assertThat(designComponent.getDesignComponentId()).isEqualTo(10);
        assertThat(designComponent.getUserEmail()).isEqualTo("modified@example.com");
        assertThat(designComponent.getComponentType()).isEqualTo(componentType);
        assertThat(designComponent.getImageUrl()).isEqualTo("https://example.com/modified.png");
        assertThat(designComponent.getIsPublic()).isFalse();
    }


}