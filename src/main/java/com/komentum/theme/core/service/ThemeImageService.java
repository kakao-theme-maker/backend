package com.komentum.theme.core.service;

import com.google.common.base.Functions;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.service.ComponentTypeService;
import com.komentum.designcomponent.service.DesignComponentService;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.global.utils.FileManager;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.dto.ThemeDesignAssetDto;
import com.komentum.theme.core.dto.ThemeDetailResponse.TypeCodeInfo;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.repository.ThemeImageRepository;
import java.util.ArrayList;
import java.util.Collection;
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
   * 테마별 COMMON_ICO_THEME 연결에서 대표 이미지 파일명을 조회한다.
   * 기존 테마의 모습을 보존하기 위해 연결된 컴포넌트가 비활성이어도 포함한다.
   * 해당 연결이나 파일명이 없는 테마는 결과 맵에서 제외한다.
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

  /**
   * 테마에 연결된 이미지와 inset 정보를 TypeCode별 상세 응답으로 구성한다.
   * 기존 구성을 유지하도록 비활성 컴포넌트도 포함하며, 연결이 없으면 빈 맵을 반환한다.
   */
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
   * 테마 이미지 연결과 테마·컴포넌트·타입을 함께 조회한다.
   * 기존 테마의 조회와 빌드에 필요한 비활성 컴포넌트도 포함한다.
   * 연결을 모두 제거한 테마도 조회할 수 있도록 연결이 없으면 빈 목록을 반환한다.
   */
  @Transactional(readOnly = true)
  public List<ThemeImage> fetchJoinThemeImagesByThemeComponentId(Integer themeComponentId) {
    return fetchJoinThemeImageMapByThemeIds(List.of(themeComponentId))
        .getOrDefault(themeComponentId, List.of());
  }

  /**
   * 테마 이미지 연결과 연관 엔티티를 조회하여 테마 ID별로 묶는다.
   * 기존 테마를 재현하기 위한 내부 조회이므로 컴포넌트의 활성 상태를 제한하지 않는다.
   * 이미지 연결이 없는 테마는 결과 맵에 포함하지 않는다.
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
   * 테마별 기존 이미지 연결을 테마 게시글 응답에 사용할 에셋 정보로 변환한다.
   * 컴포넌트가 비활성이어도 기존 테마의 파일을 사용할 수 있도록 포함하며,
   * 이미지 연결이 없는 테마는 결과 맵에 포함하지 않는다.
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

  /**
   * 원본 테마의 컴포넌트·타입 연결을 새 테마에 복사하며, 컴포넌트와 이미지 파일은 공유한다.
   * 새 연결을 만드는 작업이므로 기존 테마의 비활성 참조 보존 예외를 적용하지 않는다.
   *
   * @throws ResourceNotFoundException 원본에 비활성 컴포넌트가 포함된 경우
   */
  @Transactional
  public void copyThemeImages(ThemeComponent targetTheme,
      ThemeComponent sourceTheme) {
    List<ThemeImage> targetThemeImages = new ArrayList<>();
    Set<ThemeImage> sourceThemeImages = sourceTheme.getThemeImages();
    // 일부 연결만 복사하지 않도록 새 연결을 만들기 전에 원본 전체의 활성 상태를 검증한다.
    designComponentService.requireActiveComponents(sourceThemeImages.stream()
        .map(ThemeImage::getDesignComponent)
        .toList());
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
   * 테마 복제도 새 연결이므로 기존 테마에서 사용하던 컴포넌트라도 비활성 상태라면 허용하지 않는다.
   *
   * @param typeCodes   typeCode별 이미지 정보 맵, 존재하는 값은 null이 아니어야 한다
   * @throws ResponseStatusException   platformScope=COMMON인 TypeCode가 요청에 누락된 경우 (400 Bad Request)
   * @throws ResourceNotFoundException typeCode에 대응하는 ComponentType이나 designComponentId에 대응하는
   *                                   DesignComponent가 없거나, 요청한 컴포넌트가 비활성 상태인 경우
   */
  @Transactional
  public void createThemeImages(
      ThemeComponent targetTheme,
      Map<TypeCode, ThemeImageUpdateRequest> typeCodes
  ) {
    Map<TypeCode, ComponentType> componentTypeMap = componentTypeService.findComponentTypeMap();
    validateCommonTypeCodesPresent(componentTypeMap, typeCodes);
    Map<Integer, DesignComponent> designComponentMap = designComponentService.findMapByIdIn(
        typeCodes.values().stream()
            .map(ThemeImageUpdateRequest::getDesignComponentId)
            .collect(Collectors.toSet()));
    // 하나라도 비활성이면 연결 생성 전에 전체 요청을 거절한다.
    designComponentService.requireActiveComponents(designComponentMap.values());
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
      themeImage.update(designComponent, imageRequest);
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
      Map<TypeCode, ThemeImageUpdateRequest> typeCodes
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
   * 요청에 포함된 TypeCode의 기존 이미지 연결과 inset을 수정한다.
   * 이미지 맵이 null이거나 비어 있으면 변경하지 않고, 값이 null인 항목은 연결만 삭제한다.
   * 동일 TypeCode에서 동일 컴포넌트 ID를 유지할 때만 비활성 참조를 허용하며,
   * 다른 TypeCode로 재사용하거나 다른 컴포넌트로 교체할 때는 활성 상태를 요구한다.
   * 기존 연결이 없는 TypeCode에 새 연결을 생성하지는 않는다.
   *
   * @throws ResourceNotFoundException 요청한 컴포넌트가 없거나, 유지 대상 외의 컴포넌트가 비활성 상태인 경우
   */
  @Transactional
  public void updateThemeImages(
      int themeComponentId,
      ThemeUpdateRequest request
  ) {
    // 요청으로 받은 TypeCode : ThemeImage 정보 맵 추출
    Map<TypeCode, ThemeImageUpdateRequest> typeCodes = request.getTypeCodes();
    if (typeCodes == null || typeCodes.isEmpty()) {
      return;
    }
    // 요청으로 받은 design component 목록 조회
    Map<Integer, DesignComponent> requestedImageMap = designComponentService.findMapByIdIn(
        typeCodes.values()
            .stream()
            .filter(Objects::nonNull)
            .map(ThemeImageUpdateRequest::getDesignComponentId)
            .collect(Collectors.toSet()));
    // 기존 theme image 목록 조회
    Map<TypeCode, ThemeImage> themeImages = fetchJoinThemeImagesByThemeComponentId(
        themeComponentId).stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            Functions.identity()
        ));
    // 기존 테마의 같은 슬롯을 유지하는 경우만 예외로 삼아 비활성 에셋의 사용 범위가 넓어지는 것을 막는다.
    List<DesignComponent> newComponents = typeCodes.entrySet().stream()
        .filter(entry -> entry.getValue() != null)
        .filter(entry -> {
          ThemeImage existingImage = themeImages.get(entry.getKey());
          return existingImage == null || !Objects.equals(
              existingImage.getDesignComponent().getDesignComponentId(),
              entry.getValue().getDesignComponentId());
        })
        .map(entry -> requestedImageMap.get(entry.getValue().getDesignComponentId()))
        .toList();
    designComponentService.requireActiveComponents(newComponents);
    // 요청 데이터 기반으로 기존 theme image 수정
    typeCodes.forEach((typeCode, updateRequest) -> {
      // null은 에셋이나 파일 삭제가 아니라 해당 테마와 TypeCode 사이의 이미지 연결 제거를 뜻한다.
      if (updateRequest == null) {
        themeImageRepository.deleteByThemeComponentIdAndTypeCode(themeComponentId, typeCode);
      }
      // 값이 모두 존재하면, 기존 ThemeImage를 갱신한다
      else {
        ThemeImage themeImage = themeImages.get(typeCode);
        DesignComponent requestedImage = requestedImageMap.get(
            updateRequest.getDesignComponentId());
        if (themeImage != null && requestedImage != null) {
          themeImage.update(requestedImage, updateRequest);
        }
      }
    });
  }
}
