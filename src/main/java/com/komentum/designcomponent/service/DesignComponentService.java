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
   * 이미 업로드된 이미지 파일로 공개 DesignComponent를 생성한다. 파일 업로드와 업로드 파일 정리는 호출자가 책임진다.
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
  /**
   * 외부 단건 조회용 응답을 반환하며, 소유자나 관리자에게도 비활성 컴포넌트는 노출하지 않는다.
   *
   * @throws ResourceNotFoundException 컴포넌트가 존재하지 않거나 비활성 상태인 경우
   */
  @Transactional(readOnly = true)
  public DesignComponentDto getDesignComponentById(Integer designComponentId) {
    DesignComponent component = getEntityById(designComponentId);
    requireActiveComponents(List.of(component));
    return mapper.toDto(component);
  }

  /**
   * 전달된 컴포넌트의 활성 값이 모두 true인지 검사한다.
   * 신규 연결처럼 활성 상태가 필요한 작업에서 호출하며, 기존 참조용 내부 조회와는 분리한다.
   * 빈 컬렉션은 허용하고, 조회 결과를 걸러내거나 컴포넌트 상태를 변경하지 않는다.
   *
   * @throws ResourceNotFoundException 활성 값이 false 또는 null인 컴포넌트가 있는 경우
   */
  public void requireActiveComponents(Collection<DesignComponent> components) {
    for (DesignComponent component : components) {
      if (!Boolean.TRUE.equals(component.getIsActive())) {
        throw new ResourceNotFoundException(
            "DesignComponent not found with id: " + component.getDesignComponentId());
      }
    }
  }

  /**
   * 권한 검사와 기존 참조 복원에 사용할 엔티티를 활성 여부와 관계없이 조회한다.
   * 외부 노출이나 신규 연결 허용 여부는 호출 측에서 별도로 검증한다.
   *
   * @throws ResourceNotFoundException 해당 ID의 컴포넌트가 존재하지 않는 경우
   */
  @Transactional(readOnly = true)
  public DesignComponent getEntityById(Integer id) {
    return designComponentRepository.findByDesignComponentId(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("DesignComponent not found with id: " + id));
  }

  /**
   * 기존 테마와 게시글의 참조를 유지할 수 있도록 비활성 컴포넌트도 포함하여 일괄 조회한다.
   * ID 목록이 없으면 빈 목록을 반환하며, 신규 사용 시에는 별도로 활성 상태를 검증해야 한다.
   *
   * @throws ResourceNotFoundException 조회 결과 수와 요청 ID 수가 일치하지 않는 경우
   */
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

  /**
   * 비활성 컴포넌트를 포함한 내부 조회 결과를 ID 기준으로 찾아 쓸 수 있도록 묶는다.
   * 기존 테마의 구성 복원에 필요한 조회이므로 활성 상태를 제한하지 않는다.
   *
   * @throws ResourceNotFoundException 요청한 ID 중 존재하지 않는 컴포넌트가 있는 경우
   */
  @Transactional(readOnly = true)
  public Map<Integer, DesignComponent> findMapByIdIn(Set<Integer> ids) {
    List<DesignComponent> designComponents = findByIdIn(ids);
    return designComponents.stream()
        .collect(Collectors.toMap(
            DesignComponent::getDesignComponentId,
            Function.identity())
        );
  }

  /**
   * 공개 사용자 식별자에 해당하는 사용자의 활성 컴포넌트만 외부 응답으로 변환한다.
   */
  @Transactional(readOnly = true)
  public List<DesignComponentDto> getByPublicUserId(String publicUserId) {
    return designComponentRepository.findByUser_PublicUserIdAndIsActiveTrue(publicUserId).stream()
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
  /**
   * 소유자 또는 관리자 권한을 확인한 뒤 활성 컴포넌트의 이미지, 공개 설정과 타입을 수정한다.
   * 전달되지 않은 항목은 유지하며, 비활성 컴포넌트는 이미지 업로드 전에 거부한다.
   *
   * @throws AccessDeniedException 소유자 또는 관리자 권한이 없는 경우
   * @throws ResourceNotFoundException 컴포넌트가 없거나 권한 검사 후 비활성으로 확인되거나 요청한 타입이 없는 경우
   * @throws ResponseStatusException 요청한 컴포넌트 타입 ID가 중복된 경우
   */
  public DesignComponentDto updateDesignComponent(Integer designComponentId,
      UpdateDesignComponentRequest request, MultipartFile image) {
    DesignComponent component = getEntityById(designComponentId);

    // 비활성 컴포넌트도 권한 없는 요청은 403으로 처리하도록 권한을 먼저 검사한다.
    if (!designComponentPolicy.canUpdate(component)) {
      throw new AccessDeniedException("failed to update designComponent : invalid user or role");
    }
    requireActiveComponents(List.of(component));

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
  /**
   * 소유자 또는 관리자 권한을 확인하고 컴포넌트만 비활성화한다.
   * 기존 테마의 참조를 보존하기 위해 DB 행, 이미지 파일과 타입 연결은 삭제하지 않는다.
   * 이미 비활성이어도 권한 검사 후 정상 종료하므로 반복 삭제를 허용한다.
   *
   * @throws ResourceNotFoundException 컴포넌트가 존재하지 않는 경우
   * @throws AccessDeniedException 활성 여부와 관계없이 소유자 또는 관리자 권한이 없는 경우
   */
  public void deleteComponent(Integer designComponentId) {
    DesignComponent component = getEntityById(designComponentId);
    // 재삭제도 소유자·관리자만 허용하므로 활성 여부로 권한 검사를 건너뛰지 않는다.
    if (!designComponentPolicy.canDelete(component)) {
      throw new AccessDeniedException("failed to delete designComponent : invalid user or role");
    }
    component.deactivate();
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
      fileManager.uploadPublicFile(image.getBytes(), fileName);
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
    String fileName = "design-components_" + UUID.randomUUID() + "." + extension;
    try (InputStream is = image.getInputStream()) {
      fileManager.uploadPublicFile(is, image.getSize(), fileName);
      return fileName;
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
