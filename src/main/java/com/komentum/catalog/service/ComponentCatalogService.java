package com.komentum.catalog.service;

import com.komentum.catalog.dto.ComponentCatalogResponse;
import com.komentum.catalog.dto.ComponentSummary;
import com.komentum.catalog.dto.ComponentType;
import com.komentum.catalog.repository.ComponentCatalogRepository;
import com.komentum.theme.theme.service.ThemeImageService;
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

  @Transactional(readOnly = true)
  public List<ComponentCatalogResponse> findComponentCatalogs(Pageable pageable,
      String userIdentifier) {
    User client = userEntityFinder.findUserEntity(userIdentifier);
    List<ComponentSummary> summaries =
        componentCatalogRepository.findComponentSummaryByClient(pageable, client);
    // find theme preview image
    Map<Integer, String> themePreviewImageMap =
        themeImageService.findThemePreviewImages(
            summaries.stream()
                .filter(s -> s.getType() == ComponentType.THEME)
                .map(ComponentSummary::getId)
                .toList()
        );
    // convert to response
    return summaries.stream()
        .map(s -> {
          String preview = s.getType() == ComponentType.THEME
              ? themePreviewImageMap.get(s.getId())
              : s.getPreviewImageUrl();
          s.setPreviewImageUrl(preview);
          return ComponentCatalogResponse.of(s);
        })
        .toList();
  }
}
