package com.komentum.theme.core.service;

import com.google.common.base.Functions;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.enums.TypeCodeGroup;
import com.komentum.designcomponent.service.ComponentTypeService;
import com.komentum.designcomponent.service.DesignComponentService;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.ImageUtils;
import com.komentum.global.utils.ImageUtils.ImageSize;
import com.komentum.theme.core.domain.ImageInset;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.dto.ThemeCloneRequest.ThemeImageCloneRequest;
import com.komentum.theme.core.dto.ThemeDesignAssetDto;
import com.komentum.theme.core.dto.ThemeDetailResponse.TypeCodeInfo;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.repository.ThemeImageRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeImageService {

  private final ThemeImageRepository themeImageRepository;
  private final DesignComponentService designComponentService;
  private final ComponentTypeService componentTypeService;
  private final FileManager fileManager;

  /**
   * themeComponent ID별 테마 대표 이미지의 파일명을 조회한다.
   *
   */
  @Transactional(readOnly = true)
  public Map<Integer, String> findThemePreviewFileNames(List<Integer> themeComponentIds) {
    List<ThemeImage> themeImageList = themeImageRepository.fetchJoinByThemeComponentAndTypeCode(
        themeComponentIds,
        TypeCode.COMMON_ICO_THEME
    );
    return themeImageList.stream()
        .filter(ti -> ti.getDesignComponent().getFileName() != null)
        .collect(Collectors.toMap(
            ti -> ti.getThemeComponent().getThemeComponentId(),
            ti -> ti.getDesignComponent().getFileName(),
            (v1, v2) -> v1
        ));
  }

  @Transactional(readOnly = true)
  public String findThemePreviewFileName(Integer themeComponentId) {
    return findThemePreviewFileNames(List.of(themeComponentId)).get(themeComponentId);
  }

  /**
   * themeComponent ID별 테마 대표 이미지의 URL을 조회한다. URL은 FileManager를 통해 생성한다.
   *
   */
  @Transactional(readOnly = true)
  public Map<Integer, String> findThemePreviewImageUrls(List<Integer> themeComponentIds) {
    return findThemePreviewFileNames(themeComponentIds).entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> fileManager.resolveFilePath(entry.getValue())
        ));
  }

  /*
   * */
  @Transactional(readOnly = true)
  public String findThemePreviewImageUrl(Integer themeComponentId) {
    return findThemePreviewImageUrls(List.of(themeComponentId)).get(themeComponentId);
  }

  @Transactional(readOnly = true)
  public Map<TypeCode, TypeCodeInfo> findTypeCodeMapByThemeComponentId(Integer themeComponentId) {
    List<ThemeImage> themeImages = fetchJoinThemeImagesByThemeComponentId(
        themeComponentId);
    Map<TypeCode, TypeCodeInfo> res = themeImages.stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            ti -> TypeCodeInfo.of(ti.getDesignComponent(), ti.getComponentType(),
                ti.getImageInset(), resolveImageUrl(ti.getDesignComponent()))
        ));
    if (res.size() != TypeCode.values().length) {
      log.warn(
          "[ThemeImageService] Missing TypeCode. themeComponentId={}, actual={}, expected={}",
          themeComponentId,
          res.size(),
          TypeCode.values().length
      );
    }
    return res;
  }

  /**
   * themeComponent ID를 기반으로 ThemeImage 목록을 조회한다. 연관 엔티티(ThemeComponent, designComponent,
   * componentType)이 함께 조회된다.
   *
   */
  @Transactional(readOnly = true)
  public List<ThemeImage> fetchJoinThemeImagesByThemeComponentId(Integer themeComponentId) {
    return fetchJoinThemeImageMapByThemeIds(List.of(themeComponentId)).get(themeComponentId);
  }

  /**
   * themeComponent ID 목록을 기반으로 ThemeComponentId - ThemeImage 맵을 조회한다. 연관 엔티티(ThemeComponent,
   * designComponent, componentType)이 함께 조회된다.
   *
   */
  @Transactional(readOnly = true)
  public Map<Integer, List<ThemeImage>> fetchJoinThemeImageMapByThemeIds(
      Collection<Integer> themeIds) {
    List<ThemeImage> themeImages = themeImageRepository.fetchJoinAllByThemeComponentIds(themeIds);
    return themeImages.stream()
        .collect(Collectors.groupingBy(
            themeImage -> themeImage.getThemeComponent().getThemeComponentId()
        ));
  }

  /**
   * theme ID 리스트를 기반으로 테마별 ThemeDesignAssetDto 리스트를 조회한다
   *
   */
  @Transactional(readOnly = true)
  public Map<Integer, List<ThemeDesignAssetDto>> findThemeDesignAssetMap(
      Collection<Integer> themeIds) {
    return fetchJoinThemeImageMapByThemeIds(themeIds).entrySet()
        .stream().collect(Collectors.toMap(
            Entry::getKey,
            entry -> entry.getValue().stream()
                .map(themeImage -> ThemeDesignAssetDto.from(
                    themeImage.getComponentType(),
                    themeImage.getDesignComponent(),
                    resolveImageUrl(themeImage.getDesignComponent())
                ))
                .toList()
        ));
  }

  /**
   * designComponent의 fileName을 FileManager를 통해 이미지 URL로 변환한다. fileName이 없으면 null을 반환한다.
   *
   */
  private String resolveImageUrl(DesignComponent designComponent) {
    String fileName = designComponent.getFileName();
    return fileName == null ? null : fileManager.resolveFilePath(fileName);
  }

  @Transactional
  public void copyThemeImages(ThemeComponent targetTheme,
      ThemeComponent sourceTheme) {
    List<ThemeImage> targetThemeImages = new ArrayList<>();
    Set<ThemeImage> sourceThemeImages = sourceTheme.getThemeImages();
    for (ThemeImage sourceThemeImage : sourceThemeImages) {
      ThemeImage targetThemeImage = ThemeImage.copyOf(targetTheme, sourceThemeImage);
      targetThemeImages.add(targetThemeImage);
      targetTheme.addThemeImage(targetThemeImage);
    }
    themeImageRepository.saveAll(targetThemeImages);
  }

  /**
   * typeCode별 이미지 정보로 새로운 ThemeImage들을 생성하여 targetTheme에 추가한다. platformScope=COMMON인 TypeCode는 요청에
   * 반드시 포함되어야 하며, 특정 플랫폼 전용(ANDROID/IOS) TypeCode는 요청에 없어도 된다. 요청에 포함된 TypeCode는 targetTheme에 아직
   * 이미지가 없는 상태여야 한다.
   *
   * @param targetTheme 이미지를 추가할 대상 테마
   * @param typeCodes   typeCode별 이미지 정보 맵, 존재하는 값은 null이 아니어야 한다
   * @throws ResponseStatusException   platformScope=COMMON인 TypeCode가 요청에 누락된 경우 (400 Bad Request)
   * @throws ResourceNotFoundException typeCode에 대응하는 ComponentType이나 designComponentId에 대응하는
   *                                   DesignComponent가 존재하지 않는 경우
   */
  @Transactional
  public void createThemeImages(
      ThemeComponent targetTheme,
      Map<TypeCode, ThemeImageCloneRequest> typeCodes
  ) {
    Map<TypeCode, ComponentType> componentTypeMap = componentTypeService.findComponentTypeMap();
    validateCommonTypeCodesPresent(componentTypeMap, typeCodes);
    Map<Integer, DesignComponent> designComponentMap = designComponentService.findMapByIdIn(
        typeCodes.values().stream()
            .map(ThemeImageCloneRequest::getDesignComponentId)
            .collect(Collectors.toSet()));
    List<ThemeImage> themeImages = new ArrayList<>();
    typeCodes.forEach((typeCode, imageRequest) -> {
      ComponentType componentType = componentTypeMap.get(typeCode);
      if (componentType == null) {
        throw new ResourceNotFoundException("ComponentType not found for typeCode: " + typeCode);
      }
      DesignComponent designComponent = designComponentMap.get(imageRequest.getDesignComponentId());
      if (designComponent == null) {
        throw new ResourceNotFoundException(
            "DesignComponent not found with id: " + imageRequest.getDesignComponentId());
      }
      ThemeImage themeImage = ThemeImage.builder()
          .themeComponent(targetTheme)
          .componentType(componentType)
          .build();
      themeImage.update(designComponent, imageRequest.getInset());
      themeImages.add(themeImage);
      targetTheme.addThemeImage(themeImage);
    });
    themeImageRepository.saveAll(themeImages);
  }

  /**
   * platformScope=COMMON인 TypeCode가 요청에 누락 없이 포함되어 있는지 검증한다. 특정 플랫폼 전용(ANDROID/IOS) TypeCode는 검증
   * 대상이 아니다.
   *
   * @throws ResponseStatusException 누락된 COMMON TypeCode가 있는 경우 (400 Bad Request)
   */
  private void validateCommonTypeCodesPresent(
      Map<TypeCode, ComponentType> componentTypeMap,
      Map<TypeCode, ThemeImageCloneRequest> typeCodes
  ) {
    Set<TypeCode> missingCommonTypeCodes = componentTypeMap.entrySet().stream()
        .filter(entry -> entry.getValue().getPlatformScope() == PlatformScope.COMMON)
        .map(Entry::getKey)
        .filter(typeCode -> typeCodes.get(typeCode) == null)
        .collect(Collectors.toSet());
    if (!missingCommonTypeCodes.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "common TypeCode(s) missing in request: " + missingCommonTypeCodes);
    }
  }

  /**
   * 테마 수정 요청에 따라 TypeCode별 이미지를 수정한다. 업로드 파일이 전달된 TypeCode는 새 DesignComponent로 교체(기존 이미지가 없으면
   * 생성)하고, deleteTypeCodes에 포함된 TypeCode는 제거하며, 둘 다 아닌 TypeCode는 기존 이미지를 유지한다. 교체·제거된 기존
   * DesignComponent는 다른 테마·게시글과 공유될 수 있으므로 삭제하지 않는다.
   *
   * @param targetTheme       수정할 테마
   * @param request           이미지 정보(inset)와 제거할 TypeCode 목록이 담긴 수정 요청
   * @param uploadedFileNames TypeCode별 이미 업로드된 파일명 맵
   * @throws ResourceNotFoundException TypeCode에 대응하는 ComponentType이 존재하지 않는 경우
   */
  @Transactional
  public void updateThemeImages(
      ThemeComponent targetTheme,
      ThemeUpdateRequest request,
      Map<TypeCode, String> uploadedFileNames
  ) {
    Integer themeComponentId = targetTheme.getThemeComponentId();
    // 기존 theme image 목록 조회
    Map<TypeCode, ThemeImage> themeImages = fetchJoinThemeImagesByThemeComponentId(
        themeComponentId).stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            Functions.identity()
        ));
    // deleteTypeCodes에 포함된 ThemeImage를 삭제한다
    if (request.getDeleteTypeCodes() != null) {
      request.getDeleteTypeCodes().stream().filter(Objects::nonNull).forEach(typeCode -> {
        themeImageRepository.deleteByThemeComponentIdAndTypeCode(themeComponentId, typeCode);
        themeImages.remove(typeCode);
      });
    }
    // 업로드 파일이 전달된 ThemeImage를 새 DesignComponent로 교체한다
    if (uploadedFileNames != null && !uploadedFileNames.isEmpty()) {
      Map<TypeCode, ComponentType> componentTypeMap = componentTypeService.findComponentTypeMap();
      List<ThemeImage> newThemeImages = new ArrayList<>();
      uploadedFileNames.forEach((typeCode, fileName) -> {
        ComponentType componentType = componentTypeMap.get(typeCode);
        if (componentType == null) {
          throw new ResourceNotFoundException("ComponentType not found for typeCode: " + typeCode);
        }
        DesignComponent designComponent = designComponentService.createDesignComponent(
            targetTheme.getUser(), fileName, componentType);
        ThemeImage themeImage = themeImages.get(typeCode);
        if (themeImage == null) {
          themeImage = ThemeImage.builder()
              .themeComponent(targetTheme)
              .componentType(componentType)
              .build();
          targetTheme.addThemeImage(themeImage);
          newThemeImages.add(themeImage);
          themeImages.put(typeCode, themeImage);
        }
        themeImage.update(designComponent, null);
      });
      themeImageRepository.saveAll(newThemeImages);
    }
    // 이미지 정보(inset)를 갱신한다
    if (request.getTypeCodes() != null) {
      request.getTypeCodes().forEach((typeCode, imageRequest) -> {
        ThemeImage themeImage = themeImages.get(typeCode);
        if (themeImage != null && imageRequest != null) {
          themeImage.update(null, imageRequest.getInset());
        }
      });
    }
  }

  /**
   * 테마 수정 후 말풍선 이미지의 inset이 이미지 범위 안에 있는지 업로드 전에 검증한다. 이번 요청에서 이미지가 교체되거나 inset이 전달된
   * 말풍선만 검증한다. 검증 기준은 inset은 요청 값(없으면 기존 값), 이미지 크기는 새 파일(없으면 기존 파일)이다.
   *
   * @param themeComponentId 수정할 테마 ID
   * @param request          inset과 제거할 TypeCode 목록이 담긴 수정 요청
   * @param imageFiles       TypeCode별 교체할 이미지 파일 맵
   * @throws ResponseStatusException inset이 없거나 이미지 범위를 벗어난 경우(INVALID_IMAGE_INSET), 업로드 파일이 이미지
   *                                 포맷이 아닌 경우(INVALID_IMAGE_FORMAT) (400 Bad Request)
   */
  @Transactional(readOnly = true)
  public void validateImageInsets(
      Integer themeComponentId,
      ThemeUpdateRequest request,
      Map<TypeCode, MultipartFile> imageFiles
  ) {
    Map<TypeCode, MultipartFile> files = imageFiles == null ? Map.of() : imageFiles;
    Set<TypeCode> targetTypeCodes = findInsetValidationTargets(request, files);
    if (targetTypeCodes.isEmpty()) {
      return;
    }
    Map<TypeCode, ThemeImage> themeImages = fetchJoinThemeImagesByThemeComponentId(
        themeComponentId).stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            Functions.identity()
        ));
    for (TypeCode typeCode : targetTypeCodes) {
      MultipartFile newFile = files.get(typeCode);
      ThemeImage existingImage = themeImages.get(typeCode);
      // 새 파일도 기존 이미지도 없다면 수정되는 이미지가 없으므로 검증하지 않는다
      if (newFile == null && existingImage == null) {
        continue;
      }
      // 기존인셋-새이미지, 새인셋-새이미지, 새인셋-기존이미지에 대한 인셋 검증을 수행한다
      ImageInset inset = findFinalInset(request, typeCode, existingImage);
      ImageSize imageSize = newFile != null
          ? readUploadedImageSize(typeCode, newFile)
          : readExistingImageSize(typeCode, existingImage);
      if (inset == null || !inset.isInImageRange(imageSize.width(), imageSize.height())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "INVALID_IMAGE_INSET: inset is missing or out of image range : typeCode="
                + typeCode.getTypeCode() + ", imageSize=" + imageSize.width() + "x"
                + imageSize.height());
      }
    }
  }

  /**
   * inset 검증 대상 TypeCode를 찾는다. 이미지가 교체되거나 inset이 전달된 말풍선이 대상이며, 제거되는 이미지는 제외한다.
   */
  private Set<TypeCode> findInsetValidationTargets(ThemeUpdateRequest request,
      Map<TypeCode, MultipartFile> files) {
    Set<TypeCode> targets = new LinkedHashSet<>(files.keySet());
    if (request.getTypeCodes() != null) {
      request.getTypeCodes().forEach((typeCode, imageRequest) -> {
        if (imageRequest != null && imageRequest.getInset() != null) {
          targets.add(typeCode);
        }
      });
    }
    if (request.getDeleteTypeCodes() != null) {
      targets.removeAll(request.getDeleteTypeCodes());
    }
    targets.removeIf(typeCode -> typeCode.getTypeCodeGroup() != TypeCodeGroup.CHATROOM_BUBBLE);
    return targets;
  }

  /**
   * 수정 후 이미지에 적용될 inset을 찾는다. 요청에 inset이 있으면 요청 값, 없으면 기존 이미지의 inset이다.
   *
   * @return 적용될 inset, 요청에도 기존 이미지에도 없다면 null
   */
  private ImageInset findFinalInset(ThemeUpdateRequest request, TypeCode typeCode,
      ThemeImage existingImage) {
    ThemeImageUpdateRequest imageRequest =
        request.getTypeCodes() == null ? null : request.getTypeCodes().get(typeCode);
    if (imageRequest != null && imageRequest.getInset() != null) {
      return ImageInset.from(imageRequest.getInset());
    }
    return existingImage == null ? null : existingImage.getImageInset();
  }

  /**
   * 업로드 파일의 이미지 크기를 읽는다.
   *
   * @throws ResponseStatusException 이미지 포맷이 아닌 경우 (400 Bad Request)
   */
  private ImageSize readUploadedImageSize(TypeCode typeCode, MultipartFile file) {
    try (InputStream is = file.getInputStream()) {
      return ImageUtils.readImageSize(is).orElseThrow(() ->
          new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "INVALID_IMAGE_FORMAT: unsupported image format : " + typeCode.getTypeCode()));
    } catch (IOException e) {
      throw new UncheckedIOException("failed to read theme image file : " + typeCode, e);
    }
  }

  /**
   * 저장소에 있는 기존 이미지의 크기를 읽는다.
   *
   * @throws IllegalStateException 저장된 이미지의 크기를 읽지 못한 경우
   */
  private ImageSize readExistingImageSize(TypeCode typeCode, ThemeImage existingImage) {
    try (InputStream is = fileManager.download(existingImage.getDesignComponent().getFileName())) {
      return ImageUtils.readImageSize(is).orElseThrow(() ->
          new IllegalStateException(
              "failed to read image size of existing theme image : " + typeCode.getTypeCode()));
    } catch (IOException e) {
      throw new UncheckedIOException("failed to read theme image file : " + typeCode, e);
    }
  }
}
