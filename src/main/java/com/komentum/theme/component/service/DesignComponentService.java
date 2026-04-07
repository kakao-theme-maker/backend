package com.komentum.theme.component.service;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.FileUtils;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.domain.policy.DesignComponentPolicy;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.mapper.DesignComponentMapper;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.user.domain.User;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class DesignComponentService {

  private final DesignComponentRepository designComponentRepository;
  private final DesignComponentPolicy designComponentPolicy;
  private final DesignComponentMapper mapper;
  private final FileManager fileManager;
  private final FileUtils fileUtils;

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request,
      MultipartFile image,
      User user) {
    String imageUrl = uploadImage(image);
    try {
      DesignComponent newComponent = mapper.toEntity(request, imageUrl, user);
      return mapper.toDto(designComponentRepository.save(newComponent));
    } catch (Exception e) {
      fileUtils.deleteFileSilently(fileManager.convertUrlToFileName(imageUrl),
          String.valueOf(e));
      throw new RuntimeException("Failed to create design component", e);
    }
  }

  // READ
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer designComponentId) {
    DesignComponent component = getEntityById(designComponentId);
    return mapper.toDto(component);
  }

  @Transactional(readOnly = true)
  public DesignComponent getEntityById(Integer id) {
    return designComponentRepository.findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("DesignComponent not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getByUserEmail(String userEmail) {
    return designComponentRepository.findByUser_UserEmail(userEmail).stream()
        .map(mapper::toDto).toList();
  }

  // 페이지네이션 지원 메서드 (새로 추가)
  @Transactional(readOnly = true)
  public Page<DesignComponentDto> getAllDesignComponents(Pageable pageable) {
    return designComponentRepository.findAll(pageable)
        .map(mapper::toDto);
  }

  // UPDATE
  public DesignComponentDto updateDesignComponent(Integer designComponentId,
      UpdateDesignComponentRequest request, MultipartFile image) {
    DesignComponent component = getEntityById(designComponentId);

    // designComponentPolicy 검증
    if (!designComponentPolicy.canUpdate(component)) {
      throw new AccessDeniedException("failed to update designComponent : invalid user or role");
    }

    // 새 이미지 업로드 성공 시 이전 이미지 삭제
    String beforeImageUrl = component.getImageUrl();
    String afterImageUrl = (image != null) ? uploadImage(image) : null;
    try {
      component.update(afterImageUrl, request.getIsPublic());
      DesignComponentDto result = mapper.toDto(component);
      if (afterImageUrl != null && beforeImageUrl != null) {
        fileUtils.deleteFileSilently(fileManager.convertUrlToFileName(beforeImageUrl),
            null);
      }
      return result;
    } catch (Exception e) {
      if (afterImageUrl != null) {
        fileUtils.deleteFileSilently(fileManager.convertUrlToFileName(afterImageUrl),
            String.valueOf(e));
      }
      throw e;
    }

  }

  // DELETE
  public void deleteComponent(Integer designComponentId) {
    DesignComponent component = getEntityById(designComponentId);
    // designComponentPolicy 검증 -> 파사드에서 유저 / 정책관리는 서비스
    if (!designComponentPolicy.canDelete(component)) {
      throw new AccessDeniedException("failed to delete designComponent : invalid user or role");
    }
    String imageUrl = component.getImageUrl();
    designComponentRepository.delete(component);

    if (imageUrl != null) {
      fileUtils.deleteFileSilently(fileManager.convertUrlToFileName(imageUrl), null);
    }
  }

  private String uploadImage(MultipartFile image) {
    try {
      String extension = fileUtils.extractExtension(image.getOriginalFilename());
      String fileName = fileUtils.generateUniqueFileName(DesignComponent.class, extension);
      return fileManager.uploadFile(image.getBytes(), fileName);
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload image", e);
    }
  }


}