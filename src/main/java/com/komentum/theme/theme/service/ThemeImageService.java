package com.komentum.theme.theme.service;

import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.repository.ThemeImageRepository;
import java.util.List;
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
    return themeImages.get(0);
  }
}
