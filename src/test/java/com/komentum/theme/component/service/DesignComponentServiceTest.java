package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesignComponentServiceTest {

    @Mock
    private DesignComponentRepository designComponentRepository;

    @Mock
    private ComponentTypeRepository componentTypeRepository;

    @InjectMocks
    private DesignComponentService designComponentService;

    private ComponentType buttonType;
    private DesignComponent designComponent;
    private CreateDesignComponentRequest createRequest;

    @BeforeEach
    void setUp() {
        buttonType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼 컴포넌트")
                .iosComponentPath("/ios/button")
                .iosComponentName("ButtonComponent")
                .androidComponentPath("/android/button")
                .androidComponentName("AndroidButton")
                .sizeX(100)
                .sizeY(50)
                .build();

        designComponent = DesignComponent.builder()
                .designComponentId(1)
                .userEmail("test@example.com")
                .componentType(buttonType)
                .imageUrl("https://example.com/image.png")
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new CreateDesignComponentRequest();
        createRequest.setUserEmail("test@example.com");
        createRequest.setComponentTypeId(1);
        createRequest.setImageUrl("https://example.com/image.png");
        createRequest.setIsPublic(true);
    }

    @Test
    void createDesignComponent_WithValidRequest_ShouldReturnEntity() {
        // Given
        when(componentTypeRepository.findById(1)).thenReturn(Optional.of(buttonType));
        when(designComponentRepository.save(any(DesignComponent.class))).thenReturn(designComponent);

        // When
        DesignComponent result = designComponentService.createDesignComponent(createRequest);

        // Then
        assertThat(result.getUserEmail()).isEqualTo("test@example.com");
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.png");
        assertThat(result.getIsPublic()).isTrue();
        assertThat(result.getComponentType().getComponentTypeId()).isEqualTo(1);
        verify(componentTypeRepository).findById(1);
        verify(designComponentRepository).save(any(DesignComponent.class));
    }

    @Test
    void createDesignComponent_WithInvalidComponentTypeId_ShouldThrowException() {
        // Given
        createRequest.setComponentTypeId(999);
        when(componentTypeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> designComponentService.createDesignComponent(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ComponentType not found with id: 999");
    }

    @Test
    void getDesignComponentById_WithValidId_ShouldReturnEntity() {
        // Given
        when(designComponentRepository.findById(1)).thenReturn(Optional.of(designComponent));

        // When
        DesignComponent result = designComponentService.getDesignComponentById(1);

        // Then
        assertThat(result.getDesignComponentId()).isEqualTo(1);
        assertThat(result.getUserEmail()).isEqualTo("test@example.com");
        assertThat(result.getComponentType().getExplain()).isEqualTo("버튼 컴포넌트");
    }

    @Test
    void getDesignComponentById_WithInvalidId_ShouldThrowException() {
        // Given
        when(designComponentRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> designComponentService.getDesignComponentById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DesignComponent not found with id: 999");
    }

    @Test
    void getAllDesignComponents_ShouldReturnAllComponents() {
        // Given
        List<DesignComponent> components = List.of(designComponent);
        when(designComponentRepository.findAll()).thenReturn(components);

        // When
        List<DesignComponent> result = designComponentService.getAllDesignComponents();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesignComponentId()).isEqualTo(1);
    }

    @Test
    void updateDesignComponent_WithValidData_ShouldUpdateAndReturn() {
        // Given
        DesignComponent updateData = DesignComponent.builder()
                .userEmail("updated@example.com")
                .imageUrl("https://example.com/updated.png")
                .isPublic(false)
                .build();
        
        when(designComponentRepository.findById(1)).thenReturn(Optional.of(designComponent));
        when(designComponentRepository.save(any(DesignComponent.class))).thenReturn(designComponent);

        // When
        DesignComponent result = designComponentService.updateDesignComponent(1, updateData);

        // Then
        verify(designComponentRepository).save(designComponent);
        assertThat(result).isNotNull();
    }

    @Test
    void updateDesignComponent_WithInvalidId_ShouldThrowException() {
        // Given
        DesignComponent updateData = DesignComponent.builder().build();
        when(designComponentRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> designComponentService.updateDesignComponent(999, updateData))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DesignComponent not found with id: 999");
    }

    @Test
    void deleteDesignComponent_WithValidId_ShouldDeleteSuccessfully() {
        // Given
        when(designComponentRepository.existsById(1)).thenReturn(true);

        // When
        designComponentService.deleteDesignComponent(1);

        // Then
        verify(designComponentRepository).deleteById(1);
    }

    @Test
    void deleteDesignComponent_WithInvalidId_ShouldThrowException() {
        // Given
        when(designComponentRepository.existsById(999)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> designComponentService.deleteDesignComponent(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("DesignComponent not found with id: 999");
    }

    @Test
    void updateDesignComponent_WithComponentType_ShouldUpdateRelation() {
        // Given
        ComponentType newType = ComponentType.builder()
                .componentTypeId(2)
                .explain("아이콘 컴포넌트")
                .build();
        
        DesignComponent updateData = DesignComponent.builder()
                .componentType(newType)
                .build();
        
        when(designComponentRepository.findById(1)).thenReturn(Optional.of(designComponent));
        when(componentTypeRepository.findById(2)).thenReturn(Optional.of(newType));
        when(designComponentRepository.save(any(DesignComponent.class))).thenReturn(designComponent);

        // When
        DesignComponent result = designComponentService.updateDesignComponent(1, updateData);

        // Then
        verify(componentTypeRepository).findById(2);
        verify(designComponentRepository).save(designComponent);
        assertThat(result).isNotNull();
    }


}