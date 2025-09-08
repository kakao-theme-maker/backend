package com.komentum.theme.component.repository;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.enums.Platform;
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
class ColorStyleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ColorStyleRepository colorStyleRepository;

    @Test
    void save_ShouldPersistColorStyle() {
        // Given
        ColorStyle colorStyle = ColorStyle.builder()
                .explain("배경색 스타일")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".container")
                .stylePropsName("background-color")
                .build();

        // When
        ColorStyle savedColorStyle = colorStyleRepository.save(colorStyle);

        // Then
        assertThat(savedColorStyle.getColorTypeId()).isNotNull();
        assertThat(savedColorStyle.getExplain()).isEqualTo("배경색 스타일");
    }

    @Test
    void findById_WithValidId_ShouldReturnColorStyle() {
        // Given
        ColorStyle colorStyle = ColorStyle.builder()
                .explain("텍스트 색상")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".text")
                .stylePropsName("color")
                .build();
        ColorStyle saved = entityManager.persistAndFlush(colorStyle);

        // When
        Optional<ColorStyle> found = colorStyleRepository.findById(saved.getColorTypeId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getExplain()).isEqualTo("텍스트 색상");
    }

    @Test
    void findById_WithInvalidId_ShouldReturnEmpty() {
        // When
        Optional<ColorStyle> found = colorStyleRepository.findById(999);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllColorStyles() {
        // Given
        ColorStyle style1 = ColorStyle.builder()
                .explain("스타일1")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".style1")
                .stylePropsName("color")
                .build();
        
        ColorStyle style2 = ColorStyle.builder()
                .explain("스타일2")
                .platform(Platform.ANDROID)
                .styleSheetPath("android/colors.xml")
                .styleElementName("View")
                .stylePropsName("style2_color")
                .build();

        entityManager.persist(style1);
        entityManager.persist(style2);
        entityManager.flush();

        // When
        List<ColorStyle> colorStyles = colorStyleRepository.findAll();

        // Then
        assertThat(colorStyles).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveColorStyle() {
        // Given
        ColorStyle colorStyle = ColorStyle.builder()
                .explain("삭제할 스타일")
                .platform(Platform.IOS)
                .styleSheetPath("styles/ios.css")
                .styleElementName(".delete")
                .stylePropsName("color")
                .build();
        ColorStyle saved = entityManager.persistAndFlush(colorStyle);

        // When
        colorStyleRepository.deleteById(saved.getColorTypeId());
        entityManager.flush();

        // Then
        Optional<ColorStyle> deleted = colorStyleRepository.findById(saved.getColorTypeId());
        assertThat(deleted).isEmpty();
    }
}