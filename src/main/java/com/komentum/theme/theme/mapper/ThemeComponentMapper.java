package com.komentum.theme.theme.mapper;

import com.komentum.theme.component.dto.CreateThemeRequest;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemeImageDto;
import com.komentum.theme.theme.dto.ThemeStyleDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThemeComponentMapper {

  private final ThemeImageMapper themeImageMapper;
  private final ThemeStyleMapper themeStyleMapper;

  public ThemeComponentDto convertToDto(ThemeComponent component) {
    List<ThemeStyleDto> themeStyleDtoList = component.getThemeStyles().stream()
        .map(themeStyleMapper::convertToDto)
        .toList();
    List<ThemeImageDto> themeImageDtoList = component.getThemeImages().stream()
        .map(themeImageMapper::convertToDto)
        .toList();
    return ThemeComponentDto.builder()
        .themeComponentId(component.getThemeComponentId())
        .themeName(component.getThemeName())
        .userEmail(component.getUserEmail())
        .versionName(component.getVersionName())
        .versionNumber(component.getVersionNumber())
        .styles(themeStyleDtoList)
        .images(themeImageDtoList)
        .isPublic(component.getIsPublic())
        .isDone(component.getIsDone())
        .build();
  }

  public ThemeComponent convertToTransientEntity(CreateThemeRequest request, String versionNumber) {
    return ThemeComponent.builder()
        .isPublic(request.getIsPublic())
        .versionName(request.getVersionName())
        .versionNumber(versionNumber)
        .userEmail(request.getUserEmail())
        .themeName(request.getThemeName())
        .build();
  }
}
