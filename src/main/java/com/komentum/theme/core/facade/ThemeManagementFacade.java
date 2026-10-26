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

  /**
   * 기본 테마의 이미지 연결과 스타일을 복사하여 사용자의 새 테마를 생성한다.
   * 기본 테마의 기존 참조는 보존되지만, 새 테마에는 비활성 컴포넌트를 연결할 수 없으므로
   * 비활성 컴포넌트가 포함되어 있으면 테마 생성 전체를 롤백한다.
   *
   * @throws com.komentum.global.exception.ResourceNotFoundException 기본 테마에 비활성 컴포넌트가 포함된 경우
   */
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

  /**
   * 소유자 또는 관리자 권한을 확인한 뒤 테마의 메타데이터·이미지 연결·스타일을 함께 수정한다.
   * 비활성 컴포넌트는 동일 TypeCode와 동일 ID의 기존 연결을 유지할 때만 허용한다.
   * 이미지 검증이 실패하면 앞서 변경한 메타데이터를 포함해 전체 수정을 롤백한다.
   *
   * @throws AccessDeniedException 요청자가 테마 소유자나 관리자가 아닌 경우
   * @throws com.komentum.global.exception.ResourceNotFoundException 요청한 컴포넌트가 없거나,
   *     기존 연결 유지 대상 외의 컴포넌트가 비활성 상태인 경우
   */
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
   * 생성되는 테마의 제작자는 현재 인증된 사용자로 설정된다. 테마와 이미지 연결·스타일 행은 새로 생성하지만,
   * 연결된 디자인 컴포넌트와 이미지 파일은 공유한다.
   * platformScope=COMMON인 TypeCode/StyleCode는 요청에 반드시 포함되어야 하며, 특정 플랫폼(ANDROID/IOS) 전용
   * TypeCode/StyleCode는 요청에 없어도 된다.
   * 이미지 연결은 새로 생성되므로 원본 테마가 사용 중이어도 비활성 컴포넌트는 복제할 수 없으며,
   * 검증에 실패하면 새 테마를 포함한 생성 결과 전체를 롤백한다.
   *
   * @param publicUserId 테마 제작자로 설정할 현재 인증된 사용자의 public user ID
   * @throws ResponseStatusException 요청에 platformScope=COMMON인 TypeCode/StyleCode가 누락된 경우 (400 Bad Request)
   * @throws com.komentum.global.exception.ResourceNotFoundException 요청한 컴포넌트나 타입이 없거나,
   *     컴포넌트가 비활성 상태인 경우
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
