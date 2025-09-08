package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignComponentService {

  private final DesignComponentRepository designComponentRepository;
  private final ComponentTypeRepository componentTypeRepository;

  // DTO 변환 메서드
  private DesignComponentDto convertToDto(DesignComponent entity) {
    return DesignComponentDto.builder()
        .designComponentId(entity.getDesignComponentId())
        .userEmail(entity.getUserEmail())
        .componentType(convertComponentTypeToDto(entity.getComponentType()))
        .imageUrl(entity.getImageUrl())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .isPublic(entity.getIsPublic())
        .build();
  }

  private DesignComponentDto.ComponentTypeDto convertComponentTypeToDto(ComponentType entity) {
    if (entity == null) {
      return null;
    }

    return DesignComponentDto.ComponentTypeDto.builder()
        .componentTypeId(entity.getComponentTypeId())
        .explain(entity.getExplain())
        .platform(entity.getPlatform())
        .componentPath(entity.getComponentPath())
        .componentName(entity.getComponentName())
        .sizeX(entity.getSizeX())
        .sizeY(entity.getSizeY())
        .build();
  }

  public DesignComponent createDesignComponent(CreateDesignComponentRequest request) {
    ComponentType componentType = componentTypeRepository.findById(request.getComponentTypeId())
        .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found with id: " + request.getComponentTypeId()));

    DesignComponent newComponent = DesignComponent.builder()
        .userEmail(request.getUserEmail())
        .imageUrl(request.getImageUrl())
        .isPublic(request.getIsPublic())
        .componentType(componentType)
        .build();

    return designComponentRepository.save(newComponent);
  }

  @Transactional(readOnly = true)
  public DesignComponent getDesignComponentById(Integer id) {
    return designComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DesignComponent not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<DesignComponent> getAllDesignComponents() {
    return designComponentRepository.findAll();
  }

  public DesignComponent updateDesignComponent(Integer id, DesignComponent designComponentDetails) {
    DesignComponent designComponent = getDesignComponentById(id);

    Optional.ofNullable(designComponentDetails.getUserEmail()).ifPresent(designComponent::setUserEmail);
    Optional.ofNullable(designComponentDetails.getImageUrl()).ifPresent(designComponent::setImageUrl);
    Optional.ofNullable(designComponentDetails.getIsPublic()).ifPresent(designComponent::setIsPublic);

    if (designComponentDetails.getComponentType() != null) {
      ComponentType componentType = componentTypeRepository.findById(designComponentDetails.getComponentType().getComponentTypeId())
          .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found"));
      designComponent.setComponentType(componentType);
    }

    return designComponentRepository.save(designComponent);
  }

  public void deleteDesignComponent(Integer id) {
    if (!designComponentRepository.existsById(id)) {
      throw new ResourceNotFoundException("DesignComponent not found with id: " + id);
    }
    designComponentRepository.deleteById(id);
  }
}
