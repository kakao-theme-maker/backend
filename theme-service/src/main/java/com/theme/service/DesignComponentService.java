package com.theme.service;

import com.theme.domain.ComponentType;
import com.theme.domain.DesignComponent;
import com.theme.dto.CreateDesignComponentRequest;
import com.theme.dto.DesignComponentDto;
import com.theme.exception.ResourceNotFoundException;
import com.theme.repository.ComponentTypeRepository;
import com.theme.repository.DesignComponentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                .rgba(entity.getRgba())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isPublic(entity.getIsPublic())
                .build();
    }

    private DesignComponentDto.ComponentTypeDto convertComponentTypeToDto(ComponentType entity) {
        if (entity == null) return null;

        return DesignComponentDto.ComponentTypeDto.builder()
                .componentTypeId(entity.getComponentTypeId())
                .explain(entity.getExplain())
                .iosComponentPath(entity.getIosComponentPath())
                .iosComponentName(entity.getIosComponentName())
                .androidComponentPath(entity.getAndroidComponentPath())
                .androidComponentName(entity.getAndroidComponentName())
                .sizeX(entity.getSizeX())
                .sizeY(entity.getSizeY())
                .build();
    }

    // CREATE
    public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request) {
        // componentTypeId 추출
        Integer componentTypeId = request.getComponentTypeId();

        ComponentType componentType = componentTypeRepository.findById(componentTypeId)
                .orElseThrow(() -> new RuntimeException("ComponentType not found"));

        DesignComponent newComponent = DesignComponent.builder()
                .userEmail(request.getUserEmail())
                .imageUrl(request.getImageUrl())
                .rgba(request.getRgba())
                .isPublic(request.getIsPublic())
                .componentType(componentType)
                .build();

        return convertToDto(designComponentRepository.save(newComponent));
    }


    // READ
    @Transactional(readOnly = true)
    public DesignComponentDto getDesignComponentById(Integer id) {
        DesignComponent component = designComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DesignComponent not found"));
        return convertToDto(component);
    }

    @Transactional(readOnly = true)
    public List<DesignComponentDto> getAllDesignComponents() {
        return designComponentRepository.findAllWithComponentType().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
    public DesignComponentDto updateComponent(Integer id, DesignComponentDto request, Integer componentTypeId) {
        DesignComponent existing = designComponentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DesignComponent not found"));

        Optional.ofNullable(request.getUserEmail()).ifPresent(existing::setUserEmail);
        Optional.ofNullable(request.getImageUrl()).ifPresent(existing::setImageUrl);
        Optional.ofNullable(request.getRgba()).ifPresent(existing::setRgba);
        Optional.ofNullable(request.getIsPublic()).ifPresent(existing::setIsPublic);

        if (componentTypeId != null) {
            ComponentType type = componentTypeRepository.findById(componentTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("ComponentType not found"));
            existing.setComponentType(type);
        }

        return convertToDto(designComponentRepository.save(existing));
    }

    // DELETE
    public void deleteComponent(Integer id) {
        if (!designComponentRepository.existsById(id)) {
            throw new ResourceNotFoundException("DesignComponent not found");
        }
        designComponentRepository.deleteById(id);
    }
}
