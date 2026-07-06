package com.komentum.theme.ios.editor;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.repository.PlatformColorStyleRepository;
import com.komentum.designcomponent.repository.PlatformComponentTypeRepository;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.UserRole;
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
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
  private final IosThemeTemplateCopier iosThemeTemplateCopier;
  private final IosThemeCssEditor iosThemeCssEditor;
  private final IosThemeImageEditor iosThemeImageEditor;
  private final IosThemeSaver iosThemeSaver;

  @Transactional(readOnly = true)
  public IosThemePackageResponse makeTheme(Integer themeComponentId, CustomUserDetails userDetails) {
    Path workDir = null;
    try {
      ThemeComponent themeComponent = themeRetrieveService.getThemeEntityById(themeComponentId);
      validateAccess(themeComponent, userDetails);
      List<ThemeImage> themeImages =
          themeImageService.fetchJoinThemeImagesByThemeComponentId(themeComponentId);
      List<ThemeStyle> themeStyles =
          themeStyleService.fetchJoinThemeStylesByThemeComponentId(themeComponentId);
      List<PlatformComponentType> platformComponentTypes =
          platformComponentTypeRepository.findAllByPlatform(Platform.IOS);
      List<PlatformColorStyle> platformColorStyles =
          platformColorStyleRepository.findAllByPlatform(Platform.IOS);

      workDir = IosThemePathManager.createThemeWorkDir(themeComponentId);
      iosThemeTemplateCopier.copyTemplate(workDir);
      iosThemeCssEditor.editCss(workDir, themeComponent, themeStyles, platformColorStyles);
      iosThemeImageEditor.editImages(workDir, themeImages, platformComponentTypes);
      return iosThemeSaver.save(themeComponentId, workDir);
    } catch (AccessDeniedException e) {
      throw e;
    } catch (Exception e) {
      log.error("failed to make iOS theme package. themeComponentId={}", themeComponentId, e);
      throw new RuntimeException("failed to make iOS theme package", e);
    } finally {
      IosThemePathManager.deleteRecursivelyQuietly(workDir);
    }
  }

  private void validateAccess(ThemeComponent themeComponent, CustomUserDetails userDetails) {
    if (userDetails == null) {
      throw new AccessDeniedException("authentication is required");
    }
    if (userDetails.getUserRole() == UserRole.ADMIN) {
      return;
    }
    User currentUser = userEntityFinder.findUserEntity(userDetails.getPublicUserId());
    if (!Objects.equals(themeComponent.getUserEmail(), currentUser.getUserEmail())) {
      throw new AccessDeniedException("failed to make iOS theme package : invalid user or role");
    }
  }
}
