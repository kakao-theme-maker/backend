package com.komentum.theme.theme.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemePreviewDto;
import com.komentum.theme.theme.enums.ThemeSortType;
import com.komentum.theme.theme.mapper.ThemeComponentMapper;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.theme.repository.ThemeComponentRepositorySupport;
import com.komentum.theme.theme.service.condition.ThemeSearchCondition;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeRetrieveService {

  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeComponentMapper themeComponentMapper;
  private final ThemeImageService themeImageService;
  private final UserEntityFinder userEntityFinder;
  private final ThemeComponentRepositorySupport themeComponentRepositorySupport;

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getAllThemes(Pageable pageable) {
    return themeComponentRepository.findAll(pageable).stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getThemesByUserEmail(String userEmail, Pageable pageable) {
    return themeComponentRepository.findByUserEmail(userEmail, pageable).stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getPublicThemes(Pageable pageable) {
    return themeComponentRepository.findByIsPublicTrue(pageable).stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ThemeComponentDto getThemeById(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    return themeComponentMapper.convertToDto(themeComponent);
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getCompletedThemes(Pageable pageable) {
    List<ThemeComponent> completedThemes = themeComponentRepository.findByIsDoneTrue(pageable);
    return completedThemes.stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getCompletedThemesByUser(String userEmail, Pageable pageable) {
    List<ThemeComponent> completedThemes = themeComponentRepository.findByIsDoneTrueAndUserEmail(
        userEmail, pageable);
    return completedThemes.stream()
        .map(themeComponentMapper::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ThemeComponent getThemeEntityById(Integer id) {
    return themeComponentRepository.findById(id)
        .orElseThrow(() -> new CustomEntityNotFoundException(ThemeComponent.class, id));
  }

  @Transactional(readOnly = true)
  public List<ThemePreviewDto> findPopularThemeList(Pageable pageable, String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    ThemeSearchCondition condition = new ThemeSearchCondition();
    List<ThemeComponent> themeComponents = themeComponentRepositorySupport.findAllThemesByCondition(
        pageable,
        client,
        condition,
        List.of(ThemeSortType.PREFER_DESC)
    );
    return toPreviewDto(themeComponents);
  }


  @Transactional(readOnly = true)
  public List<ThemePreviewDto> findBookmarkedThemeList(Pageable pageable, String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    ThemeSearchCondition condition = new ThemeSearchCondition();
    condition.withBookmarked(true);
    List<ThemeComponent> themeComponents = themeComponentRepositorySupport.findAllThemesByCondition(
        pageable,
        client,
        condition,
        List.of(ThemeSortType.CREATED_DESC)
    );
    return toPreviewDto(themeComponents);
  }

  private List<ThemePreviewDto> toPreviewDto(List<ThemeComponent> themeComponents) {
    List<Integer> themeIds = themeComponents.stream()
        .map(ThemeComponent::getThemeComponentId)
        .toList();
    Map<Integer, String> themeImageMap = themeImageService.findThemePreviewImages(themeIds);
    return themeComponents.stream().map(tc -> {
      String previewImageUrl = themeImageMap.get(tc.getThemeComponentId());
      return ThemePreviewDto.from(tc, previewImageUrl);
    }).toList();
  }
}
