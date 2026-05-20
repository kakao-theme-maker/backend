package com.komentum.theme.component.service;

import com.komentum.global.utils.FileManager;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.domain.policy.DesignComponentPolicy;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.mapper.DesignComponentMapper;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.user.domain.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DesignComponentService {

  private final DesignComponentRepository designComponentRepository;
  private final ComponentTypeRepository componentTypeRepository;
  private final DesignComponentPolicy designComponentPolicy;
  private final DesignComponentMapper mapper;
  private final FileManager fileManager;

  // CREATE
  public DesignComponentDto createDesignComponent(CreateDesignComponentRequest request,
      MultipartFile image,
      User user) {
    validateImageFile(image, "image");
    List<ComponentType> componentTypes = resolveComponentTypes(request.getComponentTypeIds());
    DesignComponent saved = createDesignComponentInternal(request, user, componentTypes, image);
    try {
      designComponentRepository.flush();
      return mapper.toDto(saved);
    } catch (RuntimeException e) {
      deleteUploadedImageQuietly(saved.getImageUrl());
      throw e;
    }
  }

  public List<DesignComponentDto> createDesignComponents(CreateDesignComponentRequest request,
      List<MultipartFile> files, User user) {
    validateFiles(files);
    List<ComponentType> componentTypes = resolveComponentTypes(request.getComponentTypeIds());
    List<DesignComponent> savedComponents = new ArrayList<>();
    List<String> uploadedImageUrls = new ArrayList<>();

    try {
      for (MultipartFile file : files) {
        DesignComponent saved = createDesignComponentInternal(
            request, user, componentTypes, file);
        savedComponents.add(saved);
        uploadedImageUrls.add(saved.getImageUrl());
      }
      designComponentRepository.flush();
      return savedComponents.stream()
          .map(mapper::toDto)
          .toList();
    } catch (RuntimeException e) {
      uploadedImageUrls.forEach(this::deleteUploadedImageQuietly);
      throw e;
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
    return designComponentRepository.findByDesignComponentId(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("DesignComponent not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getByPublicUserId(String publicUserId) {
    return designComponentRepository.findByUser_PublicUserId(publicUserId).stream()
        .map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getDesignComponentsByComponentTypeId(Integer componentTypeId) {
    if (!componentTypeRepository.existsById(componentTypeId)) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "ComponentType not found with id: " + componentTypeId
      );
    }

    List<Integer> designComponentIds = designComponentRepository
        .findDesignComponentIdsByComponentTypeId(componentTypeId);
    return findDtosByOrderedIds(designComponentIds);
  }

  // 페이지네이션 지원 메서드 (새로 추가)
  @Transactional(readOnly = true)
  public Page<DesignComponentDto> getAllDesignComponents(Pageable pageable) {
    Page<Integer> designComponentIdPage = designComponentRepository.findDesignComponentIdPage(
        pageable);
    if (designComponentIdPage.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, designComponentIdPage.getTotalElements());
    }

    List<Integer> designComponentIds = designComponentIdPage.getContent();
    Map<Integer, DesignComponent> componentMap = designComponentRepository
        .findByDesignComponentIdIn(designComponentIds).stream()
        .collect(Collectors.toMap(DesignComponent::getDesignComponentId, Function.identity()));

    List<DesignComponentDto> content = designComponentIds.stream()
        .map(componentMap::get)
        .filter(Objects::nonNull)
        .map(mapper::toDto)
        .toList();

    return new PageImpl<>(content, pageable, designComponentIdPage.getTotalElements());
  }

  // UPDATE
  public DesignComponentDto updateDesignComponent(Integer designComponentId,
      UpdateDesignComponentRequest request, MultipartFile image) {
    DesignComponent component = getEntityById(designComponentId);

    // designComponentPolicy 검증
    if (!designComponentPolicy.canUpdate(component)) {
      throw new AccessDeniedException("failed to update designComponent : invalid user or role");
    }

    List<ComponentType> componentTypes = null;
    if (request.getComponentTypeIds() != null) {
      componentTypes = resolveComponentTypes(request.getComponentTypeIds());
    }

    String imageUrl = (image != null) ? uploadImage(image) : null;

    try {
      component.update(imageUrl, request.getIsPublic());
      if (componentTypes != null) {
        component.replaceComponentTypes(componentTypes);
      }
      designComponentRepository.flush();
      return mapper.toDto(component);
    } catch (RuntimeException e) {
      deleteUploadedImageQuietly(imageUrl);
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
    designComponentRepository.delete(component);
  }

  private String uploadImage(MultipartFile image) {
    try {
      String fileName =
          "design-components/" + UUID.randomUUID() + "_" + image.getOriginalFilename();
      return fileManager.uploadFile(image.getBytes(), fileName);
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload image", e);
    }

  }

  private DesignComponent createDesignComponentInternal(CreateDesignComponentRequest request,
      User user, List<ComponentType> componentTypes, MultipartFile image) {
    String imageUrl = uploadImage(image);

    try {
      DesignComponent newComponent = mapper.toEntity(request, imageUrl, user);
      newComponent.replaceComponentTypes(componentTypes);
      return designComponentRepository.save(newComponent);
    } catch (RuntimeException e) {
      deleteUploadedImageQuietly(imageUrl);
      throw e;
    }
  }

  private void validateFiles(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "files is required");
    }

    for (int i = 0; i < files.size(); i++) {
      validateImageFile(files.get(i), "files[" + i + "]");
    }
  }

  private void validateImageFile(MultipartFile image, String fieldName) {
    if (image == null || image.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          fieldName + " must not be empty");
    }
  }

  private void deleteUploadedImageQuietly(String imageUrl) {
    if (imageUrl == null) {
      return;
    }
    try {
      fileManager.deleteFile(fileManager.convertUrlToFileName(imageUrl));
    } catch (RuntimeException e) {
      log.warn("failed to cleanup uploaded image after rollback: {}", imageUrl, e);
    }
  }

  /**
   * 입력된 componentsTypeIds 를 검증하고 정규화, 엔티티 변환한다.
   *
   * @param requestedIds
   * @return
   */
  private List<ComponentType> resolveComponentTypes(List<Integer> requestedIds) {
    validateDuplicateComponentTypeIds(requestedIds);
    LinkedHashSet<Integer> uniqueRequestedIds = new LinkedHashSet<>(requestedIds);
    List<ComponentType> componentTypes = componentTypeRepository.findAllById(uniqueRequestedIds);

    Set<Integer> foundIds = componentTypes.stream()
        .map(ComponentType::getComponentTypeId)
        .collect(Collectors.toSet());
    List<Integer> missingIds = uniqueRequestedIds.stream()
        .filter(id -> !foundIds.contains(id))
        .toList();
    if (!missingIds.isEmpty()) {
      throw new ResourceNotFoundException("ComponentType not found with ids: " + missingIds);
    }

    Map<Integer, ComponentType> componentTypeMap = componentTypes.stream()
        .collect(Collectors.toMap(ComponentType::getComponentTypeId, Function.identity()));
    return uniqueRequestedIds.stream()
        .map(componentTypeMap::get)
        .toList();
  }

  /**
   * componentType의 중복 검증
   *
   * @param requestedIds
   */
  private void validateDuplicateComponentTypeIds(List<Integer> requestedIds) {
    // 이미 처리한 id 집합
    Set<Integer> seen = new LinkedHashSet<>();
    List<Integer> duplicateIds = requestedIds.stream()
        .filter(id -> !seen.add(id))
        .distinct()
        .toList();
    if (!duplicateIds.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Duplicate componentTypeIds are not allowed: " + duplicateIds
      );
    }
  }

  private List<DesignComponentDto> findDtosByOrderedIds(List<Integer> designComponentIds) {
    if (designComponentIds.isEmpty()) {
      return List.of();
    }

    Map<Integer, DesignComponent> componentMap = designComponentRepository
        .findByDesignComponentIdIn(designComponentIds).stream()
        .collect(Collectors.toMap(DesignComponent::getDesignComponentId, Function.identity()));

    return designComponentIds.stream()
        .map(componentMap::get)
        .filter(Objects::nonNull)
        .map(mapper::toDto)
        .toList();
  }

}
