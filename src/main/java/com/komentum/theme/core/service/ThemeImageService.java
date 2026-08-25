package com.komentum.theme.core.service;

import com.google.common.base.Functions;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.service.DesignComponentService;
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
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeImageService {

  private final ThemeImageRepository themeImageRepository;
  private final DesignComponentService designComponentService;

  @Transactional(readOnly = true)
  public Map<Integer, String> findThemePreviewImages(List<Integer> themeComponentIds) {
    List<ThemeImage> themeImageList = themeImageRepository.fetchJoinByThemeComponentAndTypeCode(
        themeComponentIds,
        TypeCode.PASSCODE_BACKGROUND_IMAGE
    );
    return themeImageList.stream()
        .collect(Collectors.toMap(
            ti -> ti.getThemeComponent().getThemeComponentId(),
            ti -> ti.getDesignComponent().getImageUrl(),
            (v1, v2) -> v1
        ));
  }

  @Transactional(readOnly = true)
  public String findThemePreviewImageUrl(Integer themeComponentId) {
    return findThemePreviewImages(List.of(themeComponentId)).get(themeComponentId);
  }

  @Transactional(readOnly = true)
  public Map<TypeCode, TypeCodeInfo> findTypeCodeMapByThemeComponentId(Integer themeComponentId) {
    List<ThemeImage> themeImages = fetchJoinThemeImagesByThemeComponentId(
        themeComponentId);
    Map<TypeCode, TypeCodeInfo> res = themeImages.stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            ti -> TypeCodeInfo.of(ti.getDesignComponent(), ti.getComponentType().getPlatformScope())
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
   * themeComponent ID를 기반으로 ThemeImage 목록을 조회한다.
   * 연관 엔티티(ThemeComponent, designComponent, componentType)이 함께 조회된다.
   * */
  @Transactional(readOnly = true)
  public List<ThemeImage> fetchJoinThemeImagesByThemeComponentId(Integer themeComponentId) {
    return fetchJoinThemeImageMapByThemeIds(List.of(themeComponentId)).get(themeComponentId);
  }

  /**
   * themeComponent ID 목록을 기반으로 ThemeComponentId - ThemeImage 맵을 조회한다.
   * 연관 엔티티(ThemeComponent, designComponent, componentType)이 함께 조회된다.
   * */
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
   * */
  @Transactional(readOnly = true)
  public Map<Integer, List<ThemeDesignAssetDto>> findThemeDesignAssetMap(
      Collection<Integer> themeIds) {
    return fetchJoinThemeImageMapByThemeIds(themeIds).entrySet()
        .stream().collect(Collectors.toMap(
            Entry::getKey,
            entry -> entry.getValue().stream()
                .map(themeImage -> ThemeDesignAssetDto.from(
                    themeImage.getComponentType(),
                    themeImage.getDesignComponent()
                ))
                .toList()
        ));
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
            .map(ThemeImageUpdateRequest::getDesignComponentId)
            .toList()
    );
    // 기존 theme image 목록 조회
    Map<TypeCode, ThemeImage> themeImages = fetchJoinThemeImagesByThemeComponentId(
        themeComponentId).stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            Functions.identity()
        ));
    // 요청 데이터 기반으로 기존 theme image 수정
    typeCodes.forEach((typeCode, updateRequest) -> {
      ThemeImage themeImage = themeImages.get(typeCode);
      DesignComponent requestedImage = requestedImageMap.get(updateRequest.getDesignComponentId());
      if (themeImage != null && requestedImage != null) {
        themeImage.setDesignComponent(requestedImage);
      }
    });
  }
}
