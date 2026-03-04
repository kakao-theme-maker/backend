package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
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

  // DTO 변환 메서드
  private DesignComponentDto convertToDto(DesignComponent entity) {
    return DesignComponentDto.builder()
        .designComponentId(entity.getDesignComponentId())
        .userEmail(entity.getUserEmail())
        .imageUrl(entity.getImageUrl())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .isPublic(entity.getIsPublic())
        .build();
  }

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request) {
    DesignComponent newComponent = DesignComponent.builder()
        .userEmail(request.getUserEmail())
        .imageUrl(request.getImageUrl())
        .isPublic(request.getIsPublic())
        .build();

    return convertToDto(designComponentRepository.save(newComponent));
  }

  // READ
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer designComponentId) {
    DesignComponent component = designComponentRepository.findById(designComponentId)
        .orElseThrow(
            () -> new ResourceNotFoundException("DesignComponent not found with id: " + designComponentId));
    return convertToDto(component);
  }

  @Transactional
  public DesignComponent getEntityById(Integer id) {
    return designComponentRepository.findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("DesignComponent not found with id: " + id));
  }

  // 페이지네이션 지원 메서드 (새로 추가)
  @Transactional(readOnly = true)
  public Page<DesignComponentDto> getAllDesignComponents(Pageable pageable) {
    return designComponentRepository.findAll(pageable)
        .map(this::convertToDto);
  }


  // DELETE
  public void deleteComponent(Integer designComponentId) {
    if (!designComponentRepository.existsById(designComponentId)) {
      throw new ResourceNotFoundException("DesignComponent not found with designComponentId: " + designComponentId);
    }
    designComponentRepository.deleteById(designComponentId);
  }


  // UPDATE
  public DesignComponentDto updateDesignComponent(Integer id,
      UpdateDesignComponentRequest request) {
    DesignComponent existing = designComponentRepository.findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("DesignComponent not found with id: " + id));

    Optional.ofNullable(request.getUserEmail()).ifPresent(existing::setUserEmail);
    Optional.ofNullable(request.getImageUrl()).ifPresent(existing::setImageUrl);
    Optional.ofNullable(request.getIsPublic()).ifPresent(existing::setIsPublic);

    return convertToDto(designComponentRepository.save(existing));
  }
}