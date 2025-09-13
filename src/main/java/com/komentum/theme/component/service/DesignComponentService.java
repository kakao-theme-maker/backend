package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Platform enum에 따라 iOS/Android 필드 분기 설정
    String iosComponentPath = null;
    String iosComponentName = null;
    String androidComponentPath = null;
    String androidComponentName = null;

    if (entity.getPlatform() == Platform.IOS) {
      iosComponentPath = entity.getComponentPath();
      iosComponentName = entity.getComponentName();
    } else if (entity.getPlatform() == Platform.ANDROID) {
      androidComponentPath = entity.getComponentPath();
      androidComponentName = entity.getComponentName();
    }

    return DesignComponentDto.ComponentTypeDto.builder()
        .componentTypeId(entity.getComponentTypeId())
        .explain(entity.getExplain())
        .iosComponentPath(iosComponentPath)
        .iosComponentName(iosComponentName)
        .androidComponentPath(androidComponentPath)
        .androidComponentName(androidComponentName)
        .sizeX(entity.getSizeX())
        .sizeY(entity.getSizeY())
        .build();
  }

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request) {
    Integer componentTypeId = request.getComponentTypeId();

    ComponentType componentType = componentTypeRepository.findById(componentTypeId)
        .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found with id: " + componentTypeId));

    DesignComponent newComponent = DesignComponent.builder()
        .userEmail(request.getUserEmail())
        .imageUrl(request.getImageUrl())
        .isPublic(request.getIsPublic())
        .componentType(componentType)
        .build();

    return convertToDto(designComponentRepository.save(newComponent));
  }

  // READ
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer id) {
    DesignComponent component = designComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DesignComponent not found with id: " + id));
    return convertToDto(component);
  }

  // 페이지네이션 지원 메서드 (새로 추가)
  @Transactional(readOnly = true)
  public Page<DesignComponentDto> getAllDesignComponents(Pageable pageable) {
    return designComponentRepository.findAll(pageable)
        .map(this::convertToDto);
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getByUserEmail(String userEmail) {
    return designComponentRepository.findByUserEmailWithComponentType(userEmail).stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getPublicComponents() {
    return designComponentRepository.findPublicWithComponentType().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  // UPDATE
  public DesignComponentDto updateComponent(Integer id, DesignComponentDto request,
      Integer componentTypeId) {
    DesignComponent existing = designComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DesignComponent not found with id: " + id));

    Optional.ofNullable(request.getUserEmail()).ifPresent(existing::setUserEmail);
    Optional.ofNullable(request.getImageUrl()).ifPresent(existing::setImageUrl);
    Optional.ofNullable(request.getIsPublic()).ifPresent(existing::setIsPublic);

    if (componentTypeId != null) {
      ComponentType type = componentTypeRepository.findById(componentTypeId)
          .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found with id: " + componentTypeId));
      existing.setComponentType(type);
    }

    return convertToDto(designComponentRepository.save(existing));
  }

  // DELETE
  public void deleteComponent(Integer id) {
    if (!designComponentRepository.existsById(id)) {
      throw new ResourceNotFoundException("DesignComponent not found with id: " + id);
    }
    designComponentRepository.deleteById(id);
  }

  // 하위 호환성을 위한 별칭 메소드들
  public void deleteDesignComponent(Integer id) {
    deleteComponent(id);
  }

  public DesignComponentDto updateDesignComponent(Integer id, UpdateDesignComponentRequest request) {
    DesignComponent existing = designComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DesignComponent not found with id: " + id));

    Optional.ofNullable(request.getUserEmail()).ifPresent(existing::setUserEmail);
    Optional.ofNullable(request.getImageUrl()).ifPresent(existing::setImageUrl);
    Optional.ofNullable(request.getIsPublic()).ifPresent(existing::setIsPublic);

    if (request.getComponentTypeId() != null) {
      ComponentType type = componentTypeRepository.findById(request.getComponentTypeId())
          .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found with id: " + request.getComponentTypeId()));
      existing.setComponentType(type);
    }

    return convertToDto(designComponentRepository.save(existing));
  }
}