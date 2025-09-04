package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DesignComponentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DesignComponentRepository designComponentRepository;

    private ComponentType buttonType;
    private ComponentType iconType;

    @BeforeEach
    void setUp() {
        buttonType = ComponentType.builder()
                .explain("버튼 컴포넌트")
                .iosComponentPath("/ios/button")
                .iosComponentName("ButtonComponent")
                .androidComponentPath("/android/button")
                .androidComponentName("AndroidButton")
                .sizeX(100)
                .sizeY(50)
                .build();

        iconType = ComponentType.builder()
                .explain("아이콘 컴포넌트")
                .iosComponentPath("/ios/icon")
                .iosComponentName("IconComponent")
                .androidComponentPath("/android/icon")
                .androidComponentName("AndroidIcon")
                .sizeX(24)
                .sizeY(24)
                .build();

        entityManager.persist(buttonType);
        entityManager.persist(iconType);
        entityManager.flush();
    }

    @Test
    void save_ShouldPersistDesignComponent() {
        // Given
        DesignComponent designComponent = DesignComponent.builder()
                .userEmail("test@example.com")
                .componentType(buttonType)
                .imageUrl("https://example.com/image.png")
                .isPublic(true)
                .build();

        // When
        DesignComponent saved = designComponentRepository.save(designComponent);

        // Then
        assertThat(saved.getDesignComponentId()).isNotNull();
        assertThat(saved.getUserEmail()).isEqualTo("test@example.com");
        assertThat(saved.getComponentType().getExplain()).isEqualTo("버튼 컴포넌트");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByIdWithComponentType_WithValidId_ShouldReturnWithComponentType() {
        // Given
        DesignComponent designComponent = DesignComponent.builder()
                .userEmail("test@example.com")
                .componentType(buttonType)
                .imageUrl("https://example.com/image.png")
                .isPublic(true)
                .build();
        DesignComponent saved = entityManager.persistAndFlush(designComponent);

        // When
        Optional<DesignComponent> found = designComponentRepository
                .findByIdWithComponentType(saved.getDesignComponentId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getComponentType()).isNotNull();
        assertThat(found.get().getComponentType().getExplain()).isEqualTo("버튼 컴포넌트");
    }

    @Test
    void findByUserEmailWithComponentType_ShouldReturnUserComponents() {
        // Given
        DesignComponent component1 = DesignComponent.builder()
                .userEmail("user1@example.com")
                .componentType(buttonType)
                .imageUrl("https://example.com/image1.png")
                .isPublic(true)
                .build();

        DesignComponent component2 = DesignComponent.builder()
                .userEmail("user1@example.com")
                .componentType(iconType)
                .imageUrl("https://example.com/image2.png")
                .isPublic(false)
                .build();

        entityManager.persist(component1);
        entityManager.persist(component2);
        entityManager.flush();

        // When
        List<DesignComponent> userComponents = designComponentRepository
                .findByUserEmailWithComponentType("user1@example.com");

        // Then
        assertThat(userComponents).hasSize(2);
        userComponents.forEach(component -> {
            assertThat(component.getUserEmail()).isEqualTo("user1@example.com");
            assertThat(component.getComponentType()).isNotNull();
        });
    }

    @Test
    void findPublicWithComponentType_ShouldReturnOnlyPublicComponents() {
        // Given
        DesignComponent publicComponent = DesignComponent.builder()
                .userEmail("user1@example.com")
                .componentType(buttonType)
                .imageUrl("https://example.com/public.png")
                .isPublic(true)
                .build();

        DesignComponent privateComponent = DesignComponent.builder()
                .userEmail("user2@example.com")
                .componentType(iconType)
                .imageUrl("https://example.com/private.png")
                .isPublic(false)
                .build();

        entityManager.persist(publicComponent);
        entityManager.persist(privateComponent);
        entityManager.flush();

        // When
        List<DesignComponent> publicComponents = designComponentRepository.findPublicWithComponentType();

        // Then
        assertThat(publicComponents).hasSize(1);
        assertThat(publicComponents.get(0).getIsPublic()).isTrue();
        assertThat(publicComponents.get(0).getComponentType()).isNotNull();
    }
}