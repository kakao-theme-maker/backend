package com.komentum.theme.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.mapper.ThemeComponentMapper;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.theme.core.service.ThemeManageService;
import com.komentum.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ThemeManageServiceUnitTest {

  private static final Integer THEME_COMPONENT_ID = 1;

  @Mock
  private ThemeComponentRepository themeComponentRepository;

  @Mock
  private ThemeStyleRepository themeStyleRepository;

  @Mock
  private ThemeImageRepository themeImageRepository;

  @Mock
  private ThemeComponentMapper themeComponentMapper;

  @Mock
  private OwnerAdminPolicy ownerAdminPolicy;

  @InjectMocks
  private ThemeManageService themeManageService;

  private User owner;
  private ThemeComponent theme;

  /**
   * 삭제 권한이 거부되는 경우를 검증할 테마를 준비한다.
   */
  @BeforeEach
  void setUp() {
    owner = User.builder().publicUserId("theme-owner").build();
    theme = ThemeComponent.builder()
        .themeComponentId(THEME_COMPONENT_ID)
        .user(owner)
        .build();
  }

  /**
   * 권한이 거부되면 테마와 연관 데이터의 삭제를 시작하지 않는지 검증한다.
   */
  @Test
  @DisplayName("테마 삭제 권한이 없으면 어떤 데이터도 삭제하지 않는다")
  void deleteTheme_accessDenied_doesNotDelete() {
    given(themeComponentRepository.findById(THEME_COMPONENT_ID)).willReturn(Optional.of(theme));
    given(ownerAdminPolicy.validate(owner)).willReturn(false);

    assertThatThrownBy(() -> themeManageService.deleteTheme(THEME_COMPONENT_ID))
        .isInstanceOf(AccessDeniedException.class);

    verify(ownerAdminPolicy).validate(owner);
    verify(themeComponentRepository, never()).delete(any(ThemeComponent.class));
    verifyNoInteractions(themeStyleRepository, themeImageRepository, themeComponentMapper);
  }
}
