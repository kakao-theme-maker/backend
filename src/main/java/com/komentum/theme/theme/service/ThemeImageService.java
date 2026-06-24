package com.komentum.theme.theme.service;

import com.google.common.base.Functions;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.TypeCode;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.dto.ThemeDetailResponse.TypeCodeInfo;
import com.komentum.theme.theme.dto.ThemeUpdateRequest;
import com.komentum.theme.theme.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        TypeCode.COMMON_ICO_THEME
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
    List<ThemeImage> themeImages = themeImageRepository.fetchJoinAllByThemeComponentId(
        themeComponentId);
    Map<TypeCode, TypeCodeInfo> res = themeImages.stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            ti -> TypeCodeInfo.of(ti.getDesignComponent())
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

  @Transactional(readOnly = true)
  public List<ThemeImage> fetchJoinThemeImagesByThemeComponentId(Integer themeComponentId) {
    return themeImageRepository.fetchJoinAllByThemeComponentId(themeComponentId);
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
      Integer themeComponentId,
      ThemeUpdateRequest request
  ) {
    // 요청으로 받은 TypeCode : ThemeImage 정보 맵 추출
    Map<TypeCode, ThemeImageUpdateRequest> typeCodes = request.getTypeCodes();
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
