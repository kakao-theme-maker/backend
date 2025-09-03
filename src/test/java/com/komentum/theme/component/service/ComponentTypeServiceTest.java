package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.repository.ComponentTypeRepository;
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
class ComponentTypeServiceTest {

    @Mock
    private ComponentTypeRepository componentTypeRepository;

    @InjectMocks
    private ComponentTypeService componentTypeService;

    private ComponentType buttonComponentType;
    private ComponentType iconComponentType;

    @BeforeEach
    void setUp() {
        buttonComponentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼 컴포넌트")
                .iosComponentPath("/ios/button")
                .iosComponentName("ButtonComponent")
                .androidComponentPath("/android/button")
                .androidComponentName("AndroidButton")
                .sizeX(100)
                .sizeY(50)
                .build();

        iconComponentType = ComponentType.builder()
                .componentTypeId(2)
                .explain("아이콘 컴포넌트")
                .iosComponentPath("/ios/icon")
                .iosComponentName("IconComponent")
                .androidComponentPath("/android/icon")
                .androidComponentName("AndroidIcon")
                .sizeX(24)
                .sizeY(24)
                .build();
    }

    @Test
    void createComponentType_ShouldReturnSavedComponentType() {
        // Given
        ComponentType newComponentType = ComponentType.builder()
                .explain("새로운 컴포넌트")
                .build();
        when(componentTypeRepository.save(any(ComponentType.class))).thenReturn(buttonComponentType);

        // When
        ComponentType result = componentTypeService.createComponentType(newComponentType);

        // Then
        assertThat(result).isEqualTo(buttonComponentType);
        verify(componentTypeRepository).save(newComponentType);
    }

    @Test
    void getComponentTypeById_WithValidId_ShouldReturnComponentType() {
        // Given
        when(componentTypeRepository.findById(1)).thenReturn(Optional.of(buttonComponentType));

        // When
        ComponentType result = componentTypeService.getComponentTypeById(1);

        // Then
        assertThat(result).isEqualTo(buttonComponentType);
        assertThat(result.getExplain()).isEqualTo("버튼 컴포넌트");
    }

    @Test
    void getComponentTypeById_WithInvalidId_ShouldThrowException() {
        // Given
        when(componentTypeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> componentTypeService.getComponentTypeById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ComponentType not found with id: 999");
    }

    @Test
    void getAllComponentTypes_ShouldReturnAllComponentTypes() {
        // Given
        List<ComponentType> allTypes = List.of(buttonComponentType, iconComponentType);
        when(componentTypeRepository.findAll()).thenReturn(allTypes);

        // When
        List<ComponentType> result = componentTypeService.getAllComponentTypes();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(buttonComponentType, iconComponentType);
    }

    @Test
    void updateComponentType_WithValidData_ShouldUpdateAndReturn() {
        // Given
        ComponentType updateData = ComponentType.builder()
                .explain("업데이트된 설명")
                .sizeX(200)
                .build();
        
        when(componentTypeRepository.findById(1)).thenReturn(Optional.of(buttonComponentType));
        when(componentTypeRepository.save(any(ComponentType.class))).thenReturn(buttonComponentType);

        // When
        ComponentType result = componentTypeService.updateComponentType(1, updateData);

        // Then
        verify(componentTypeRepository).save(buttonComponentType);
        assertThat(result).isEqualTo(buttonComponentType);
    }

    @Test
    void updateComponentType_WithInvalidId_ShouldThrowException() {
        // Given
        ComponentType updateData = ComponentType.builder()
                .explain("업데이트된 설명")
                .build();
        when(componentTypeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> componentTypeService.updateComponentType(999, updateData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ComponentType not found with id: 999");
    }

    @Test
    void deleteComponentType_WithValidId_ShouldDeleteSuccessfully() {
        // Given
        when(componentTypeRepository.existsById(1)).thenReturn(true);

        // When
        componentTypeService.deleteComponentType(1);

        // Then
        verify(componentTypeRepository).deleteById(1);
    }

    @Test
    void deleteComponentType_WithInvalidId_ShouldThrowException() {
        // Given
        when(componentTypeRepository.existsById(999)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> componentTypeService.deleteComponentType(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ComponentType not found with id: 999");
    }
}