package com.komentum.theme.ios.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.PlatformColorStyleRepository;
import com.komentum.designcomponent.repository.PlatformComponentTypeRepository;
import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.service.ThemeImageService;
import com.komentum.theme.core.service.ThemeRetrieveService;
import com.komentum.theme.core.service.ThemeStyleService;
import com.komentum.theme.ios.dto.IosThemePackageResponse;
import com.komentum.user.domain.User;
import com.komentum.user.service.UserEntityFinder;
import java.nio.file.Path;
import java.util.List;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class IosThemeMakerTest {

  @Mock
  private ThemeRetrieveService themeRetrieveService;
  @Mock
  private ThemeImageService themeImageService;
  @Mock
  private ThemeStyleService themeStyleService;
  @Mock
  private PlatformComponentTypeRepository platformComponentTypeRepository;
  @Mock
  private PlatformColorStyleRepository platformColorStyleRepository;
  @Mock
  private UserEntityFinder userEntityFinder;
  @Mock
  private OwnerAdminPolicy ownerAdminPolicy;
  @Mock
  private IosThemeTemplateExtractor iosThemeTemplateExtractor;
  @Mock
  private IosThemeCssEditor iosThemeCssEditor;
  @Mock
  private IosThemeImageEditor iosThemeImageEditor;
  @Mock
  private IosThemeSaver iosThemeSaver;

  @InjectMocks
  private IosThemeMaker iosThemeMaker;

  @Test
  void makeTheme_returnsSavedPackageUrl() throws Exception {
    int themeComponentId = 7;
    String ownerEmail = "owner@test.com";
    String packageUrl = "https://cdn.example.com/theme.ktheme";
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(themeComponentId)
        .userEmail(ownerEmail)
        .build();
    User themeOwner = User.builder()
        .publicUserId("owner-public-id")
        .userEmail(ownerEmail)
        .build();
    when(themeRetrieveService.getThemeEntityById(themeComponentId)).thenReturn(themeComponent);
    when(userEntityFinder.findUserEntityByEmail(ownerEmail)).thenReturn(themeOwner);
    when(ownerAdminPolicy.validate(themeOwner)).thenReturn(true);
    when(themeImageService.fetchJoinThemeImagesByThemeComponentId(themeComponentId))
        .thenReturn(List.of());
    when(themeStyleService.fetchJoinThemeStylesByThemeComponentId(themeComponentId))
        .thenReturn(List.of());
    when(platformComponentTypeRepository.fetchJoinAllByPlatform(Platform.IOS)).thenReturn(List.of());
    when(platformColorStyleRepository.fetchJoinAllByPlatform(Platform.IOS)).thenReturn(List.of());
    when(iosThemeSaver.save(eq(themeComponentId), any(Path.class)))
        .thenReturn(packageUrl);

    String result = iosThemeMaker.makeTheme(themeComponentId);

    assertThat(result).isEqualTo(packageUrl);
  }

  @Test
  void makeTheme_deniesBeforeFileProcessingWhenOwnerAdminPolicyRejects() {
    // given
    int themeComponentId = 7;
    String ownerEmail = "owner@test.com";
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(themeComponentId)
        .userEmail(ownerEmail)
        .build();
    User themeOwner = User.builder()
        .publicUserId("owner-public-id")
        .userEmail(ownerEmail)
        .build();
    when(themeRetrieveService.getThemeEntityById(themeComponentId)).thenReturn(themeComponent);
    when(userEntityFinder.findUserEntityByEmail(ownerEmail)).thenReturn(themeOwner);
    when(ownerAdminPolicy.validate(themeOwner)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> iosThemeMaker.makeTheme(themeComponentId))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("invalid user or role");
    verify(userEntityFinder).findUserEntityByEmail(ownerEmail);
    verify(ownerAdminPolicy).validate(themeOwner);
    verifyNoInteractions(
        iosThemeTemplateExtractor,
        iosThemeCssEditor,
        iosThemeImageEditor,
        iosThemeSaver
    );
  }

  @Test
  void makeTheme_wrapsMissingOwnerAsPackageFailure() {
    // given
    int themeComponentId = 7;
    String ownerEmail = "missing-owner@test.com";
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(themeComponentId)
        .userEmail(ownerEmail)
        .build();
    RuntimeException ownerLookupFailure = new RuntimeException("user not found");
    when(themeRetrieveService.getThemeEntityById(themeComponentId)).thenReturn(themeComponent);
    when(userEntityFinder.findUserEntityByEmail(ownerEmail)).thenThrow(ownerLookupFailure);

    // when
    Throwable thrown = catchThrowable(() -> iosThemeMaker.makeTheme(themeComponentId));

    // then
    assertThat(thrown)
        .isInstanceOf(RuntimeException.class)
        .hasMessage("failed to make iOS theme package");
    assertThat(thrown.getCause()).isSameAs(ownerLookupFailure);
    verifyNoInteractions(
        ownerAdminPolicy,
        iosThemeTemplateExtractor,
        iosThemeCssEditor,
        iosThemeImageEditor,
        iosThemeSaver
    );
  }

  @Test
  @SuppressWarnings("unchecked")
  void makeTheme_excludesDataExclusiveToOtherPlatforms() throws Exception {
    // given
    int themeComponentId = 9;
    String ownerEmail = "owner@test.com";
    ThemeComponent themeComponent = ThemeComponent.builder()
        .themeComponentId(themeComponentId)
        .userEmail(ownerEmail)
        .build();
    User themeOwner = User.builder()
        .publicUserId("owner-public-id")
        .userEmail(ownerEmail)
        .build();
    ColorStyle commonColorStyle = ColorStyle.builder()
        .colorStyleId(1)
        .styleCode(StyleCode.MAINVIEW_STYLE_BACKGROUND_COLOR)
        .platformScope(PlatformScope.COMMON)
        .build();
    ColorStyle iosColorStyle = ColorStyle.builder()
        .colorStyleId(2)
        .styleCode(StyleCode.FEATURE_STYLE_TEXT_COLOR)
        .platformScope(PlatformScope.IOS)
        .build();
    ColorStyle androidOnlyColorStyle = ColorStyle.builder()
        .colorStyleId(3)
        .styleCode(StyleCode.TABBAR_STYLE_BACKGROUND_COLOR)
        .platformScope(PlatformScope.ANDROID)
        .build();
    ThemeStyle commonStyle = ThemeStyle.builder().colorStyle(commonColorStyle).color("#111111")
        .build();
    ThemeStyle iosStyle = ThemeStyle.builder().colorStyle(iosColorStyle).color("#222222").build();
    ThemeStyle androidOnlyStyle = ThemeStyle.builder().colorStyle(androidOnlyColorStyle)
        .color("#333333").build();
    ComponentType commonType = ComponentType.builder()
        .componentTypeId(1)
        .typeCode(TypeCode.PASSCODE_BACKGROUND_IMAGE)
        .platformScope(PlatformScope.COMMON)
        .build();
    ComponentType androidOnlyType = ComponentType.builder()
        .componentTypeId(2)
        .typeCode(TypeCode.CHAT_ROOM_BACKGROUND_IMAGE)
        .platformScope(PlatformScope.ANDROID)
        .build();
    ThemeImage commonImage = ThemeImage.builder().componentType(commonType).build();
    ThemeImage androidOnlyImage = ThemeImage.builder().componentType(androidOnlyType).build();
    // stub
    when(themeRetrieveService.getThemeEntityById(themeComponentId)).thenReturn(themeComponent);
    when(userEntityFinder.findUserEntityByEmail(ownerEmail)).thenReturn(themeOwner);
    when(ownerAdminPolicy.validate(themeOwner)).thenReturn(true);
    when(themeStyleService.fetchJoinThemeStylesByThemeComponentId(themeComponentId))
        .thenReturn(List.of(commonStyle, iosStyle, androidOnlyStyle));
    when(themeImageService.fetchJoinThemeImagesByThemeComponentId(themeComponentId))
        .thenReturn(List.of(commonImage, androidOnlyImage));
    when(iosThemeSaver.save(eq(themeComponentId), any()))
        .thenReturn(IosThemePackageResponse.builder().themeComponentId(themeComponentId).build());
    // when
    iosThemeMaker.makeTheme(themeComponentId);
    // then : Android 전용 데이터(androidOnlyStyle, androidOnlyImage)는 제외되고 공통 + iOS 전용 데이터만 iOS 에디터에 전달된다
    ArgumentCaptor<List<ThemeStyle>> styleCaptor = ArgumentCaptor.forClass(List.class);
    verify(iosThemeCssEditor).editCss(any(), eq(themeComponent), styleCaptor.capture(), any());
    assertThat(styleCaptor.getValue()).containsExactlyInAnyOrder(commonStyle, iosStyle);

    ArgumentCaptor<List<ThemeImage>> imageCaptor = ArgumentCaptor.forClass(List.class);
    verify(iosThemeImageEditor).editImages(any(), imageCaptor.capture(), any());
    assertThat(imageCaptor.getValue()).containsExactly(commonImage);
  }
}
