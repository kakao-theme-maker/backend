package com.komentum.designcomponent.service;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.domain.policy.DesignComponentPolicy;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.mapper.DesignComponentMapper;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.designcomponent.repository.DesignComponentRepositorySupport;
import com.komentum.global.enums.FileExtension;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.ImageUtils;
import com.komentum.user.domain.User;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
  private final DesignComponentRepositorySupport designComponentRepositorySupport;

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
      deleteUploadedImageQuietly(saved.getFileName());
      throw e;
    }
  }

  public List<DesignComponentDto> createDesignComponents(CreateDesignComponentRequest request,
      List<MultipartFile> files, User user) {
    validateFiles(files);
    List<ComponentType> componentTypes = resolveComponentTypes(request.getComponentTypeIds());
    List<DesignComponent> savedComponents = new ArrayList<>();
    List<String> uploadedFileNames = new ArrayList<>();

    try {
      for (MultipartFile file : files) {
        DesignComponent saved = createDesignComponentInternal(
            request, user, componentTypes, file);
        savedComponents.add(saved);
        uploadedFileNames.add(saved.getFileName());
      }
      designComponentRepository.flush();
      return savedComponents.stream()
          .map(mapper::toDto)
          .toList();
    } catch (RuntimeException e) {
      uploadedFileNames.forEach(this::deleteUploadedImageQuietly);
      throw e;
    }
  }

  /**
   * 이미 업로드된 이미지 파일로 비공개 DesignComponent를 생성한다. 파일 업로드와 업로드 파일 정리는 호출자가 책임진다.
   *
   * @param owner         DesignComponent 소유자
   * @param fileName      저장소에 업로드된 파일명
   * @param componentType 이미지가 사용될 ComponentType
   * @return 저장된 DesignComponent
   * @throws IllegalArgumentException 파라미터가 null이거나 fileName이 공백인 경우
   */
  public DesignComponent createDesignComponent(User owner, String fileName,
      ComponentType componentType) {
    if (owner == null || componentType == null || fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException(
          "failed to create design component : owner, fileName, componentType are required");
    }
    DesignComponent newComponent = mapper.toEntity(
        CreateDesignComponentRequest.builder().isPublic(true).build(), fileName, owner);
    newComponent.replaceComponentTypes(List.of(componentType));
    return designComponentRepository.save(newComponent);
  }

  /**
   * TypeCode별 이미지를 업로드하고 TypeCode별 업로드 파일명을 반환한다. 트랜잭션 없이 수행되며, 모든 파일의 실제 포맷(매직 바이트)을 먼저
   * 검증한 뒤 업로드한다. 업로드 도중 하나라도 실패하면 이번에 업로드한 파일을 모두 삭제하고 예외를 다시 던진다.
   *
   * @param images TypeCode별 업로드할 이미지, null이면 빈 맵을 반환한다
   * @return TypeCode별 저장소에 업로드된 파일명 맵
   * @throws ResponseStatusException 지원하지 않는 이미지 포맷인 경우 (400 Bad Request, INVALID_IMAGE_FORMAT)
   * @throws RuntimeException        파일을 읽거나 업로드하지 못한 경우
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public Map<TypeCode, String> uploadImages(Map<TypeCode, MultipartFile> images) {
    Map<TypeCode, String> uploadedFileNames = new EnumMap<>(TypeCode.class);
    if (images == null || images.isEmpty()) {
      return uploadedFileNames;
    }
    Map<TypeCode, String> extensions = new EnumMap<>(TypeCode.class);
    images.forEach((typeCode, image) -> extensions.put(typeCode, detectImageExtension(image)));
    try {
      images.forEach((typeCode, image) ->
          uploadedFileNames.put(typeCode, uploadImage(image, extensions.get(typeCode))));
      return uploadedFileNames;
    } catch (RuntimeException e) {
      deleteUploadedImages(uploadedFileNames.values());
      throw e;
    }
  }

  /**
   * 업로드한 이미지 파일들을 삭제한다. 보상 처리용이므로 삭제 실패는 로그만 남긴다.
   */
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void deleteUploadedImages(Collection<String> fileNames) {
    if (fileNames == null) {
      return;
    }
    fileNames.forEach(this::deleteUploadedImageQuietly);
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
  public List<DesignComponent> findByIdIn(Collection<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    List<DesignComponent> res = designComponentRepository.findByDesignComponentIdIn(ids);
    if (res.size() != ids.size()) {
      throw new ResourceNotFoundException("DesignComponent not found with id in post id list");
    }
    return res;
  }

  @Transactional(readOnly = true)
  public Map<Integer, DesignComponent> findMapByIdIn(Set<Integer> ids) {
    List<DesignComponent> designComponents = findByIdIn(ids);
    return designComponents.stream()
        .collect(Collectors.toMap(
            DesignComponent::getDesignComponentId,
            Function.identity())
        );
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getByPublicUserId(String publicUserId) {
    return designComponentRepository.findByUser_PublicUserId(publicUserId).stream()
        .map(mapper::toDto).toList();
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> getDesignComponentsByComponentTypeId(Integer componentTypeId) {
    if (!componentTypeRepository.existsById(componentTypeId)) {
      throw new ResourceNotFoundException(
          "ComponentType not found with id: " + componentTypeId);
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

  @Transactional(readOnly = true)
  public List<DesignComponent> findUploadedDesignComponents(User client) {
    if (client == null) {
      throw new IllegalArgumentException("invalid client info : client is null");
    }
    return designComponentRepositorySupport.findUploadedDesignComponents(client);
  }

  @Transactional(readOnly = true)
  public List<DesignComponentDto> findBookmarkedDesignComponents(String publicUserId) {
    if (publicUserId == null) {
      throw new IllegalArgumentException(
          "[DesignComponentService] invalid client info : publicUserId is null");
    }
    List<DesignComponent> targetDesignComponents = designComponentRepositorySupport.findBookmarkedDesignComponents(
        publicUserId);
    return targetDesignComponents.stream()
        .map(mapper::toDto)
        .toList();
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

    String fileName = (image != null) ? uploadImage(image) : null;

    try {
      component.update(fileName, request.getIsPublic());
      if (componentTypes != null) {
        component.replaceComponentTypes(componentTypes);
      }
      designComponentRepository.flush();
      return mapper.toDto(component);
    } catch (RuntimeException e) {
      deleteUploadedImageQuietly(fileName);
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

  /**
   * 이미지를 업로드하고, 저장소에 저장된 파일명을 반환한다.
   *
   * @throws RuntimeException 이미지 바이트를 읽거나 업로드하지 못한 경우
   */
  private String uploadImage(MultipartFile image) {
    try {
      String fileName =
          "design-components_" + UUID.randomUUID() + "_" + image.getOriginalFilename();
      fileManager.uploadFile(image.getBytes(), fileName);
      return fileName;
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload image", e);
    }

  }

  /**
   * 실제 포맷 기준 확장자로 이미지를 업로드하고, 저장소에 저장된 파일명을 반환한다.
   *
   * @throws RuntimeException 이미지를 읽거나 업로드하지 못한 경우
   */
  private String uploadImage(MultipartFile image, String extension) {
    try {
      return fileManager.uploadAndGetFileName(image.getInputStream(), image.getSize(),
          "design-components_" + UUID.randomUUID(), extension);
    } catch (IOException e) {
      throw new RuntimeException("Failed to upload image", e);
    }
  }

  /**
   * 이미지의 실제 포맷(매직 바이트)을 판별해 저장 확장자를 반환한다. Content-Type 헤더나 파일 확장자는 신뢰하지 않으며, 테마 빌드에서
   * 사용하는 FileExtension 포맷(PNG)만 허용한다.
   *
   * @throws ResponseStatusException 지원하지 않는 이미지 포맷인 경우 (400 Bad Request)
   * @throws RuntimeException        이미지를 읽지 못한 경우
   */
  private String detectImageExtension(MultipartFile image) {
    try (InputStream is = image.getInputStream()) {
      return ImageUtils.detectImageExtension(is)
          .map(FileExtension::getExtension)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "INVALID_IMAGE_FORMAT: unsupported image format : " + image.getName()));
    } catch (IOException e) {
      throw new RuntimeException("Failed to read image", e);
    }
  }

  private DesignComponent createDesignComponentInternal(CreateDesignComponentRequest request,
      User user, List<ComponentType> componentTypes, MultipartFile image) {
    String fileName = uploadImage(image);

    try {
      DesignComponent newComponent = mapper.toEntity(request, fileName, user);
      newComponent.replaceComponentTypes(componentTypes);
      return designComponentRepository.save(newComponent);
    } catch (RuntimeException e) {
      deleteUploadedImageQuietly(fileName);
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

  private void deleteUploadedImageQuietly(String fileName) {
    if (fileName == null) {
      return;
    }
    try {
      fileManager.deleteFile(fileName);
    } catch (RuntimeException e) {
      log.warn("failed to cleanup uploaded image after rollback: {}", fileName, e);
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
