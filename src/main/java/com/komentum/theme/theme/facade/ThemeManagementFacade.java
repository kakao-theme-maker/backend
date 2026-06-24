package com.komentum.theme.theme.facade;

import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemeDetailResponse;
import com.komentum.theme.theme.dto.ThemeUpdateRequest;
import com.komentum.theme.theme.service.ThemeImageService;
import com.komentum.theme.theme.service.ThemeManageService;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import com.komentum.theme.theme.service.ThemeStyleService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeManagementFacade {

  private final ThemeManageService themeManageService;
  private final ThemeImageService themeImageService;
  private final ThemeStyleService themeStyleService;
  private final ThemeRetrieveService themeRetrieveService;
  private final UserEntityFinder userEntityFinder;
  private final DesignComponentService designComponentService;

  @Transactional
  public ThemeDetailResponse createThemeFromDefault(String publicUserId) {
    User targetUser = userEntityFinder.findUserEntity(publicUserId);
    // generate theme component
    ThemeComponent targetTheme = themeManageService.createNewTheme(targetUser);
    // find default theme component
    ThemeComponent defaultThemeComponent = themeRetrieveService.findDefaultTheme();
    // copy default theme images
    themeImageService.copyThemeImages(targetTheme, defaultThemeComponent);
    // copy default theme styles
    themeStyleService.copyThemeStyles(targetTheme, defaultThemeComponent);
    // return theme detail response
    return themeRetrieveService.findThemeDetail(targetTheme.getThemeComponentId());
  }

  @Transactional
  public void updateTheme(Integer themeComponentId, ThemeUpdateRequest request) {
    // update theme meta data
    ThemeComponent targetTheme = themeRetrieveService.getThemeEntityById(themeComponentId);
    targetTheme.update(request);
    // update theme images
    themeImageService.updateThemeImages(themeComponentId, request);
    // update theme styles
    themeStyleService.updateThemeStyles(themeComponentId, request.getStyleCodes());
  }
}
