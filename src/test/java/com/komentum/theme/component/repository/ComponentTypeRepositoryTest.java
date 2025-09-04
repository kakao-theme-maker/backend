package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ComponentType;
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
class ComponentTypeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ComponentTypeRepository componentTypeRepository;

    @Test
    void save_ShouldPersistComponentType() {
        // Given
        ComponentType componentType = ComponentType.builder()
                .explain("버튼 컴포넌트")
                .iosComponentPath("/ios/button")
                .iosComponentName("ButtonComponent")
                .androidComponentPath("/android/button")
                .androidComponentName("AndroidButton")
                .sizeX(100)
                .sizeY(50)
                .build();

        // When
        ComponentType saved = componentTypeRepository.save(componentType);

        // Then
        assertThat(saved.getComponentTypeId()).isNotNull();
        assertThat(saved.getExplain()).isEqualTo("버튼 컴포넌트");
        assertThat(saved.getSizeX()).isEqualTo(100);
        assertThat(saved.getSizeY()).isEqualTo(50);
    }

    @Test
    void findById_WithValidId_ShouldReturnComponentType() {
        // Given
        ComponentType componentType = ComponentType.builder()
                .explain("아이콘 컴포넌트")
                .iosComponentPath("/ios/icon")
                .iosComponentName("IconComponent")
                .androidComponentPath("/android/icon")
                .androidComponentName("AndroidIcon")
                .sizeX(24)
                .sizeY(24)
                .build();
        ComponentType saved = entityManager.persistAndFlush(componentType);

        // When
        Optional<ComponentType> found = componentTypeRepository.findById(saved.getComponentTypeId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getExplain()).isEqualTo("아이콘 컴포넌트");
        assertThat(found.get().getIosComponentName()).isEqualTo("IconComponent");
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        // When
        Optional<ComponentType> found = componentTypeRepository.findById(999);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllComponentTypes() {
        // Given
        ComponentType buttonType = ComponentType.builder()
                .explain("버튼")
                .iosComponentPath("/ios/button")
                .sizeX(100)
                .sizeY(50)
                .build();

        ComponentType iconType = ComponentType.builder()
                .explain("아이콘")
                .iosComponentPath("/ios/icon")
                .sizeX(24)
                .sizeY(24)
                .build();

        entityManager.persist(buttonType);
        entityManager.persist(iconType);
        entityManager.flush();

        // When
        List<ComponentType> all = componentTypeRepository.findAll();

        // Then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(ComponentType::getExplain)
                .containsExactlyInAnyOrder("버튼", "아이콘");
    }

    @Test
    void deleteById_ShouldRemoveComponentType() {
        // Given
        ComponentType componentType = ComponentType.builder()
                .explain("삭제할 컴포넌트")
                .iosComponentPath("/ios/delete")
                .sizeX(50)
                .sizeY(50)
                .build();
        ComponentType saved = entityManager.persistAndFlush(componentType);

        // When
        componentTypeRepository.deleteById(saved.getComponentTypeId());
        entityManager.flush();

        // Then
        Optional<ComponentType> deleted = componentTypeRepository.findById(saved.getComponentTypeId());
        assertThat(deleted).isEmpty();
    }
}