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

}
