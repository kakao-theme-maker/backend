package com.komentum.theme.core.service;

import com.google.common.base.Functions;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeDetailResponse.StyleCodeInfo;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import com.komentum.theme.core.repository.ThemeStyleRepository;
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
public class ThemeStyleService {

  private final ThemeStyleRepository themeStyleRepository;

  @Transactional(readOnly = true)
  public Map<StyleCode, StyleCodeInfo> findStyleCodeMapByThemeComponentId(
      Integer themeComponentId) {
    List<ThemeStyle> themeStyles = themeStyleRepository.fetchJoinAllByThemeComponentId(
        themeComponentId);
    Map<StyleCode, StyleCodeInfo> res = themeStyles.stream()
        .collect(Collectors.toMap(
            ts -> ts.getColorStyle().getStyleCode(),
            ts -> StyleCodeInfo.of(ts.getColor())
        ));
    if (res.size() != StyleCode.values().length) {
      log.warn(
          "[ThemeStyleService] Missing StyleCode. themeComponentId={}, actual={}, expected={}",
          themeComponentId,
          res.size(),
          StyleCode.values().length
      );
    }
    return res;
  }

  @Transactional(readOnly = true)
  public List<ThemeStyle> findByThemeComponentId(Integer themeComponentId) {
    return themeStyleRepository.findByThemeComponentId(themeComponentId);
  }

  @Transactional(readOnly = true)
  public List<ThemeStyle> fetchJoinThemeStylesByThemeComponentId(Integer themeComponentId) {
    return themeStyleRepository.fetchJoinAllByThemeComponentId(themeComponentId);
  }

  @Transactional
  public void copyThemeStyles(ThemeComponent targetTheme, ThemeComponent sourceTheme) {
    List<ThemeStyle> targetThemeStyles = new ArrayList<>();
    Set<ThemeStyle> sourceThemeStyles = sourceTheme.getThemeStyles();
    for (ThemeStyle sourceThemeStyle : sourceThemeStyles) {
      ThemeStyle targetThemeStyle = ThemeStyle.copyOf(targetTheme, sourceThemeStyle);
      targetThemeStyles.add(targetThemeStyle);
      targetTheme.addThemeStyle(targetThemeStyle);
    }
    themeStyleRepository.saveAll(targetThemeStyles);
  }

  /**
   * 요청으로 전달된 StyleCode별 color/alpha 값으로 기존 ThemeStyle을 덮어쓴다.
   * color와 alpha는 항상 함께 존재해야 하며, updateRequestMap이 비어있으면 아무 것도 하지 않는다.
   *
   * @throws ResourceNotFoundException 요청에 포함된 StyleCode에 대응하는 ThemeStyle이 존재하지 않는 경우
   * @throws IllegalArgumentException  요청 항목의 color 또는 alpha가 null이거나 alpha가 0~100 범위를 벗어난 경우
   */
  @Transactional
  public void updateThemeStyles(
      int themeComponentId,
      Map<StyleCode, ThemeStyleUpdateRequest> updateRequestMap) {
    if (updateRequestMap == null || updateRequestMap.isEmpty()) {
      return;
    }
    Map<StyleCode, ThemeStyle> themeStyleMap = themeStyleRepository
        .fetchJoinAllByThemeComponentId(themeComponentId)
        .stream().collect(Collectors.toMap(
            ts -> ts.getColorStyle().getStyleCode(),
            Functions.identity())
        );
    updateRequestMap.forEach((styleCode, updateRequest) -> {
      if (updateRequest == null) {
        throw new IllegalArgumentException("style update request must not be null: " + styleCode);
      }
      ThemeStyle themeStyle = themeStyleMap.get(styleCode);
      if (themeStyle == null) {
        throw new ResourceNotFoundException(
            "ThemeStyle not found for styleCode=" + styleCode
                + ", themeComponentId=" + themeComponentId);
      }
      themeStyle.updateColorAndAlpha(updateRequest.getColor(), updateRequest.getAlpha());
    });
  }
}
