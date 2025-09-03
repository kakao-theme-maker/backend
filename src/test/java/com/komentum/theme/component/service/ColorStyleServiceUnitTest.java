package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColorStyleServiceUnitTest {

    @Mock
    private ColorStyleRepository colorStyleRepository;

    @InjectMocks
    private ColorStyleService colorStyleService;

    private ColorStyle backgroundColorStyle;
    private ColorStyle textColorStyle;

    @BeforeEach
    void setUp() {
        backgroundColorStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("배경색 설정")
                .iosStyleName(".container|background-color")
                .androidStyleName("background_color")
                .build();

        textColorStyle = ColorStyle.builder()
                .colorTypeId(2)
                .explain("텍스트 색상 설정")
                .iosStyleName(".text|color")
                .androidStyleName("text_color")
                .build();
    }

    @Test
    void createColorStyle_ShouldReturnSavedColorStyle() {
        // Given
        ColorStyle newColorStyle = ColorStyle.builder()
                .explain("새로운 색상 스타일")
                .build();
        when(colorStyleRepository.save(any(ColorStyle.class))).thenReturn(backgroundColorStyle);

        // When
        ColorStyle result = colorStyleService.createColorStyle(newColorStyle);

        // Then
        assertThat(result).isEqualTo(backgroundColorStyle);
        verify(colorStyleRepository).save(newColorStyle);
    }

    @Test
    void getColorStyleById_WithValidId_ShouldReturnColorStyle() {
        // Given
        when(colorStyleRepository.findById(1)).thenReturn(Optional.of(backgroundColorStyle));

        // When
        ColorStyle result = colorStyleService.getColorStyleById(1);

        // Then
        assertThat(result).isEqualTo(backgroundColorStyle);
        assertThat(result.getExplain()).isEqualTo("배경색 설정");
    }

    @Test
    void getColorStyleById_WithInvalidId_ShouldThrowException() {
        // Given
        when(colorStyleRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> colorStyleService.getColorStyleById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ColorStyle not found with id: 999");
    }

    @Test
    void getAllColorStyles_ShouldReturnAllStyles() {
        // Given
        List<ColorStyle> allStyles = List.of(backgroundColorStyle, textColorStyle);
        when(colorStyleRepository.findAll()).thenReturn(allStyles);

        // When
        List<ColorStyle> result = colorStyleService.getAllColorStyles();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(backgroundColorStyle, textColorStyle);
    }

    @Test
    void updateColorStyle_WithValidData_ShouldUpdateAndReturn() {
        // Given
        ColorStyle updateData = ColorStyle.builder()
                .explain("업데이트된 설명")
                .iosStyleName(".updated|color")
                .build();
        
        when(colorStyleRepository.findById(1)).thenReturn(Optional.of(backgroundColorStyle));
        when(colorStyleRepository.save(any(ColorStyle.class))).thenReturn(backgroundColorStyle);

        // When
        ColorStyle result = colorStyleService.updateColorStyle(1, updateData);

        // Then
        verify(colorStyleRepository).save(backgroundColorStyle);
        assertThat(result).isEqualTo(backgroundColorStyle);
    }

    @Test
    void updateColorStyle_WithInvalidId_ShouldThrowException() {
        // Given
        ColorStyle updateData = ColorStyle.builder()
                .explain("업데이트된 설명")
                .build();
        when(colorStyleRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> colorStyleService.updateColorStyle(999, updateData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ColorStyle not found with id: 999");
    }

    @Test
    void deleteColorStyle_WithValidId_ShouldDeleteSuccessfully() {
        // Given
        when(colorStyleRepository.existsById(1)).thenReturn(true);

        // When
        colorStyleService.deleteColorStyle(1);

        // Then
        verify(colorStyleRepository).deleteById(1);
    }

    @Test
    void deleteColorStyle_WithInvalidId_ShouldThrowException() {
        // Given
        when(colorStyleRepository.existsById(999)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> colorStyleService.deleteColorStyle(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ColorStyle not found with id: 999");
    }

    @Test
    void updateColorStyle_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        // Given
        ColorStyle existingStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("기존 설명")
                .iosStyleName(".old|color")
                .androidStyleName("old_color")
                .build();

        ColorStyle partialUpdate = ColorStyle.builder()
                .explain("새로운 설명")
                .build();

        when(colorStyleRepository.findById(1)).thenReturn(Optional.of(existingStyle));
        when(colorStyleRepository.save(any(ColorStyle.class))).thenReturn(existingStyle);

        // When
        ColorStyle result = colorStyleService.updateColorStyle(1, partialUpdate);

        // Then
        verify(colorStyleRepository).save(existingStyle);
        assertThat(existingStyle.getExplain()).isEqualTo("새로운 설명");
    }
}