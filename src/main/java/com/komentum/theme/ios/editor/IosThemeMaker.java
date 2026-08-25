package com.komentum.theme.ios.editor;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.repository.PlatformColorStyleRepository;
import com.komentum.designcomponent.repository.PlatformComponentTypeRepository;
import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.global.exception.CustomEntityNotFoundException;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.theme.core.service.ThemeRetrieveService;
import com.komentum.theme.core.service.ThemeStyleService;
import com.komentum.theme.ios.dto.IosThemePackageResponse;
import com.komentum.theme.ios.utils.IosThemePathManager;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IosThemeMaker {

  private final ThemeRetrieveService themeRetrieveService;
  private final ThemeImageService themeImageService;
  private final ThemeStyleService themeStyleService;
  private final PlatformComponentTypeRepository platformComponentTypeRepository;
  private final PlatformColorStyleRepository platformColorStyleRepository;
  private final UserEntityFinder userEntityFinder;
  private final OwnerAdminPolicy ownerAdminPolicy;
  private final IosThemeTemplateExtractor iosThemeTemplateExtractor;
  private final IosThemeCssEditor iosThemeCssEditor;
  private final IosThemeImageEditor iosThemeImageEditor;
  private final IosThemeSaver iosThemeSaver;

  public IosThemePackageResponse makeTheme(Integer themeComponentId) {
    Path workDir = null;
    try {
      ThemeComponent themeComponent = themeRetrieveService.getThemeEntityById(themeComponentId);
      validateAccess(themeComponent);
      // 연관 엔티티를 fetch join으로 먼저 조회한 뒤,
      // 이미지 다운로드·파일 처리·업로드는 DB 트랜잭션 밖에서 수행한다.
      // 공통 데이터와 iOS 전용 데이터만 사용하고, Android 전용 데이터는 제외한다.
      List<ThemeImage> themeImages =
          themeImageService.fetchJoinThemeImagesByThemeComponentId(themeComponentId).stream()
              .filter(themeImage -> themeImage.getComponentType().getPlatformScope()
                  .supports(Platform.IOS))
              .toList();
      List<ThemeStyle> themeStyles =
          themeStyleService.fetchJoinThemeStylesByThemeComponentId(themeComponentId).stream()
              .filter(themeStyle -> themeStyle.getColorStyle().getPlatformScope()
                  .supports(Platform.IOS))
              .toList();
      List<PlatformComponentType> platformComponentTypes =
          platformComponentTypeRepository.fetchJoinAllByPlatform(Platform.IOS);
      List<PlatformColorStyle> platformColorStyles =
          platformColorStyleRepository.fetchJoinAllByPlatform(Platform.IOS);

      workDir = IosThemePathManager.createThemeWorkDir(themeComponentId);
      iosThemeTemplateExtractor.extractTemplate(workDir);
      iosThemeCssEditor.editCss(workDir, themeComponent, themeStyles, platformColorStyles);
      iosThemeImageEditor.editImages(workDir, themeImages, platformComponentTypes);
      return iosThemeSaver.save(themeComponentId, workDir);
    } catch (AccessDeniedException e) {
      throw e;
    } catch (CustomEntityNotFoundException e) {
      throw new ResourceNotFoundException("Theme not found with id: " + themeComponentId);
    } catch (Exception e) {
      log.error("failed to make iOS theme package. themeComponentId={}", themeComponentId, e);
      throw new RuntimeException("failed to make iOS theme package", e);
    } finally {
      IosThemePathManager.deleteRecursivelyQuietly(workDir);
    }
  }

  private void validateAccess(ThemeComponent themeComponent) {
    User themeOwner = userEntityFinder.findUserEntityByEmail(themeComponent.getUserEmail());
    if (!ownerAdminPolicy.validate(themeOwner)) {
      throw new AccessDeniedException("failed to make iOS theme package : invalid user or role");
    }
  }
}
