package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.domain.policy.DesignComponentPolicy;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignComponentService {

  private final DesignComponentRepository designComponentRepository;
  private final DesignComponentPolicy designComponentPolicy;

  // DTO 변환 메서드
  private DesignComponentDto convertToDto(DesignComponent entity) {
    return DesignComponentDto.builder()
        .designComponentId(entity.getDesignComponentId())
        .publicUserId(entity.getUser().getPublicUserId())
        .imageUrl(entity.getImageUrl())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .isPublic(entity.getIsPublic())
        .build();
  }

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request,
      User user) {
    DesignComponent newComponent = DesignComponent.builder()
        .user(user)
        .imageUrl(request.getImageUrl())
        .isPublic(request.getIsPublic())
        .build();

    return convertToDto(designComponentRepository.save(newComponent));
  }

  // READ
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer designComponentId) {
    DesignComponent component = getEntityById(designComponentId);
    return convertToDto(component);
  }

  @Transactional(readOnly = true)
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
  public void deleteComponent(Integer designComponentId, User owner) {
    DesignComponent component = getEntityById(designComponentId);
    // designComponentPolicy 검증 -> 파사드에서 유저 / 정책관리는 서비스
    if (!designComponentPolicy.canDelete(owner)) {
      throw new AccessDeniedException("failed to delete designComponent : invalid user or role");
    }
    designComponentRepository.delete(component);
  }


  // UPDATE
  public DesignComponentDto updateDesignComponent(Integer designComponentId,
      UpdateDesignComponentRequest request, User owner) {
    DesignComponent component = getEntityById(designComponentId);

    // designComponentPolicy 검증
    if (!designComponentPolicy.canUpdate(owner)) {
      throw new AccessDeniedException("failed to update designComponent : invalid user or role");
    }

    component.update(
        request.getImageUrl(),
        request.getIsPublic()
    );
    return convertToDto(component);
  }
}