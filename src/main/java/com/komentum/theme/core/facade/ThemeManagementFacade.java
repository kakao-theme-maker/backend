package com.komentum.theme.core.facade;

import com.komentum.global.domain.policy.AdminPolicy;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.theme.core.service.ThemeManageService;
import com.komentum.theme.core.service.ThemeRetrieveService;
import com.komentum.theme.core.service.ThemeStyleService;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
  private final AdminPolicy adminPolicy;

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
  public void updateTheme(Integer themeComponentId, ThemeUpdateRequest request,
      String userIdentifier) {
    // check entity update policy
    ThemeComponent targetTheme = themeRetrieveService.getThemeEntityById(themeComponentId);
    User client = userEntityFinder.findUserEntity(userIdentifier);
    // Entity 소유자가 아니고, Admin 사용자도 아니라면 403 예외를 던진다
    // TODO : 추후 Theme 내 userEmail 대신 public user id를 저장한다면 변경하기
    if (!client.getUserEmail().equals(targetTheme.getUserEmail()) && !adminPolicy.validate()) {
      throw new AccessDeniedException("failed to update theme : invalid user");
    }
    // update theme meta data
    targetTheme.update(request);
    // update theme images
    themeImageService.updateThemeImages(themeComponentId, request);
    // update theme styles
    themeStyleService.updateThemeStyles(themeComponentId, request.getStyleCodes());
  }
}
