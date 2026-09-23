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
      Map<TypeCode, ThemeImageUpdateRequest> typeCodes
  ) {
    Map<TypeCode, ComponentType> componentTypeMap = componentTypeService.findComponentTypeMap();
    validateCommonTypeCodesPresent(componentTypeMap, typeCodes);
    Map<Integer, DesignComponent> designComponentMap = designComponentService.findMapByIdIn(
        typeCodes.values().stream()
            .map(ThemeImageUpdateRequest::getDesignComponentId)
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
    // 요청 데이터 기반으로 기존 theme image 수정
    typeCodes.forEach((typeCode, updateRequest) -> {
      // updateRequest가 null이면 기존 ThemeImage를 삭제한다
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
