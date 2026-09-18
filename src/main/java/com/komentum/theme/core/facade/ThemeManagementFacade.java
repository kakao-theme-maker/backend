package com.komentum.theme.core.facade;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.ThemeCloneRequest;
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
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ThemeManagementFacade {

  private final ThemeManageService themeManageService;
  private final ThemeImageService themeImageService;
  private final ThemeStyleService themeStyleService;
  private final ThemeRetrieveService themeRetrieveService;
  private final UserEntityFinder userEntityFinder;
  private final OwnerAdminPolicy ownerAdminPolicy;

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
    // check entity update policy
    ThemeComponent targetTheme = themeRetrieveService.getThemeEntityById(themeComponentId);
    // Entity 소유자가 아니고, Admin 사용자도 아니라면 403 예외를 던진다
    if (!ownerAdminPolicy.validate(targetTheme.getUser())) {
      throw new AccessDeniedException("failed to update theme : invalid user");
    }
    // update theme meta data
    targetTheme.update(request);
    // update theme images
    themeImageService.updateThemeImages(themeComponentId, request);
    // update theme styles
    themeStyleService.updateThemeStyles(themeComponentId, request.getStyleCodes());
  }

  /**
   * 요청받은 이미지/색상 정보를 기반으로 새로운 테마를 복제 생성한다.
   * 생성되는 테마의 제작자는 현재 인증된 사용자로 설정되며, 원본이 되는 다른 테마의 데이터와는 독립적으로 생성된다.
   * platformScope=COMMON인 TypeCode/StyleCode는 요청에 반드시 포함되어야 하며, 특정 플랫폼(ANDROID/IOS) 전용
   * TypeCode/StyleCode는 요청에 없어도 된다.
   *
   * @param request      복제할 이미지/색상 정보가 담긴 요청 DTO
   * @param publicUserId 테마 제작자로 설정할 현재 인증된 사용자의 public user ID
   * @return 생성된 테마의 상세 정보
   * @throws ResponseStatusException 요청에 platformScope=COMMON인 TypeCode/StyleCode가 누락된 경우 (400 Bad Request)
   */
  @Transactional
  public ThemeDetailResponse cloneTheme(ThemeCloneRequest request, String publicUserId) {
    User client = userEntityFinder.findUserEntity(publicUserId);
    ThemeComponent clonedTheme = themeManageService.createNewTheme(client, request.getThemeName());
    themeImageService.createThemeImages(clonedTheme, request.getTypeCodes());
    themeStyleService.createThemeStyles(clonedTheme, request.getStyleCodes());
    return themeRetrieveService.findThemeDetail(clonedTheme.getThemeComponentId());
  }
}
