package com.komentum.theme.theme.service;

import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.mapper.ThemeComponentMapper;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
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

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getAllThemes(Pageable pageable) {
    return convertToDtosWithPreviewImages(themeComponentRepository.findAll(pageable).getContent());
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getThemesByUserEmail(String userEmail, Pageable pageable) {
    return convertToDtosWithPreviewImages(themeComponentRepository.findByUserEmail(userEmail,
        pageable));
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getPublicThemes(Pageable pageable) {
    return convertToDtosWithPreviewImages(themeComponentRepository.findByIsPublicTrue(pageable));
  }

  @Transactional(readOnly = true)
  public ThemeComponentDto getThemeById(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    return convertToDtoWithPreviewImage(themeComponent, themeImageService.findThemePreviewImageUrl(
        id));
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getCompletedThemes(Pageable pageable) {
    List<ThemeComponent> completedThemes = themeComponentRepository.findByIsDoneTrue(pageable);
    return convertToDtosWithPreviewImages(completedThemes);
  }

  @Transactional(readOnly = true)
  public List<ThemeComponentDto> getCompletedThemesByUser(String userEmail, Pageable pageable) {
    List<ThemeComponent> completedThemes = themeComponentRepository.findByIsDoneTrueAndUserEmail(
        userEmail, pageable);
    return convertToDtosWithPreviewImages(completedThemes);
  }

  @Transactional(readOnly = true)
  public ThemeComponent getThemeEntityById(Integer id) {
    return themeComponentRepository.findById(id)
        .orElseThrow(() -> new CustomEntityNotFoundException(ThemeComponent.class, id));
  }

  private List<ThemeComponentDto> convertToDtosWithPreviewImages(
      List<ThemeComponent> themeComponents) {
    Map<Integer, String> previewImages = themeImageService.findThemePreviewImages(
        themeComponents.stream()
            .map(ThemeComponent::getThemeComponentId)
            .toList());
    return themeComponents.stream()
        .map(themeComponent -> convertToDtoWithPreviewImage(
            themeComponent, previewImages.get(themeComponent.getThemeComponentId())))
        .collect(Collectors.toList());
  }

  private ThemeComponentDto convertToDtoWithPreviewImage(
      ThemeComponent themeComponent,
      String previewImageUrl) {
    ThemeComponentDto themeComponentDto = themeComponentMapper.convertToDto(themeComponent);
    themeComponentDto.setPreviewImageUrl(previewImageUrl);
    return themeComponentDto;
  }
}
