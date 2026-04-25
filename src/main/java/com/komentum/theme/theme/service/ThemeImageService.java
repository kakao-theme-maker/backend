package com.komentum.theme.theme.service;

import com.komentum.post.consts.ThemeBoardConsts;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeImageService {

  private final ThemeImageRepository themeImageRepository;

  @Transactional(readOnly = true)
  public ThemeImage findByThemeComponentAndComponentTypeName(
      ThemeComponent themeComponent,
      String componentTypeName) {
    List<ThemeImage> themeImages = themeImageRepository.findByThemeComponentAndComponentType_ComponentName(
        themeComponent,
        componentTypeName);
    if (themeImages.isEmpty()) {
      throw new EntityNotFoundException(
          "ThemeImage not found for componentTypeName : " + componentTypeName);
    }
    return themeImages.get(0);
  }

  @Transactional(readOnly = true)
  public Map<Integer, String> findThemePreviewImages(List<Integer> themeComponentIds) {
    List<ThemeImage> themeImageList = themeImageRepository.fetchJoinByThemeComponentAndComponentInfo(
        themeComponentIds,
        ThemeBoardConsts.DEFAULT_COMPONENT_TYPE_NAME,
        ThemeBoardConsts.DEFAULT_COMPONENT_TYPE_PATH
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
}
