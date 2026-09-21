package com.komentum.catalog.service;

import com.komentum.catalog.dto.ComponentCatalogResponse;
import com.komentum.catalog.dto.ComponentSummary;
import com.komentum.catalog.dto.ComponentType;
import com.komentum.catalog.repository.ComponentCatalogRepository;
import com.komentum.global.utils.FileManager;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ComponentCatalogService {

  private final ComponentCatalogRepository componentCatalogRepository;
  private final ThemeImageService themeImageService;
  private final UserEntityFinder userEntityFinder;
  private final FileManager fileManager;

  @Transactional(readOnly = true)
  public List<ComponentCatalogResponse> findComponentCatalogs(Pageable pageable,
      String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    List<ComponentSummary> summaries =
        componentCatalogRepository.findComponentSummaryByClient(pageable, client);
    // find theme preview image
    Map<Integer, String> themePreviewImageMap =
        themeImageService.findThemePreviewImageUrls(
            summaries.stream()
                .filter(s -> s.getType() == ComponentType.THEME)
                .map(ComponentSummary::getId)
                .toList()
        );
    // convert to response
    return summaries.stream()
        .map(s -> {
          String imageUrl = s.getPreviewImageFileName() == null ? null
              : fileManager.resolveFilePath(s.getPreviewImageFileName());
          String preview = s.getType() == ComponentType.THEME
              ? themePreviewImageMap.get(s.getId())
              : imageUrl;
          return ComponentCatalogResponse.of(s, preview);
        })
        .toList();
  }
}
