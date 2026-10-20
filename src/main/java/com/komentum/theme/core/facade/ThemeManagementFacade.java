package com.komentum.theme.core.facade;

import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.service.DesignComponentService;
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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
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
  private final DesignComponentService designComponentService;
  private final TransactionTemplate transactionTemplate;

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
   * 테마 메타데이터, 이미지, 색상을 수정한다. 새 이미지는 트랜잭션 밖에서 업로드하고, 트랜잭션에서 TypeCode별 이미지 참조를 교체·제거한다.
   * 트랜잭션(커밋)이 실패하면 이번에 업로드한 파일을 삭제한다. 교체·제거된 기존 이미지는 다른 테마·게시글과 공유될 수 있으므로 삭제하지 않는다.
   *
   * @param themeComponentId 수정할 테마 ID
   * @param request          이미지 외 수정 정보와 제거할 TypeCode 목록
   * @param imageFiles       TypeCode별 교체할 이미지 파일 맵
   * @throws AccessDeniedException   테마 소유자도, Admin 사용자도 아닌 경우 (403 Forbidden)
   * @throws ResponseStatusException 이미지 포맷이 올바르지 않은 경우 (400 Bad Request)
   */
  public void updateTheme(Integer themeComponentId, ThemeUpdateRequest request,
      Map<TypeCode, MultipartFile> imageFiles) {
    // Entity 소유자가 아니고, Admin 사용자도 아니라면 403 예외를 던진다
    ThemeComponent targetTheme = themeRetrieveService.getThemeEntityById(themeComponentId);
    if (!ownerAdminPolicy.validate(targetTheme.getUser())) {
      throw new AccessDeniedException("failed to update theme : invalid user");
    }
    // 새 이미지 업로드 ( 실패 시 이번에 업로드한 파일은 서비스에서 삭제 )
    Map<TypeCode, String> uploadedFileNames = designComponentService.uploadImages(imageFiles);
    // DB 처리 + 커밋, 실패 시 업로드한 파일 롤백
    try {
      transactionTemplate.executeWithoutResult(status -> {
        // update theme meta data
        targetTheme.update(request);
        // update theme images
        themeImageService.updateThemeImages(targetTheme, request, uploadedFileNames);
        // update theme styles
        themeStyleService.updateThemeStyles(themeComponentId, request.getStyleCodes());
      });
    } catch (RuntimeException e) {
      designComponentService.deleteUploadedImages(uploadedFileNames.values());
      throw e;
    }
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
