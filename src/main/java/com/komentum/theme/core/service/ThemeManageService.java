package com.komentum.theme.core.service;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.global.exception.ResourceNotFoundException;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.ThemeComponentDto;
import com.komentum.theme.core.mapper.ThemeComponentMapper;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.user.domain.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThemeManageService {

  private final ThemeComponentRepository themeComponentRepository;
  private final ThemeStyleRepository themeStyleRepository;
  private final ThemeImageRepository themeImageRepository;
  private final ThemeComponentMapper themeComponentMapper;
  private final OwnerAdminPolicy ownerAdminPolicy;

  @Transactional
  public ThemeComponent createNewTheme(User targetUser) {
    String randomThemeName = "theme_" + UUID.randomUUID();
    return themeComponentRepository.save(
        ThemeComponent.builder()
            .themeName(randomThemeName)
            .user(targetUser)
            .versionName(randomThemeName + ".0.0.1")
            .versionNumber("1")
            .isPublic(true)
            .isDone(false)
            .build()
    );
  }

  /**
   * 지정된 이름으로 새로운 테마를 생성한다. 이름이 없으면 임의의 이름을 부여한다.
   *
   * @param targetUser 테마 제작자로 설정할 사용자
   * @param themeName  테마 이름, null이거나 공백이면 임의의 이름을 사용한다
   * @return 생성된 ThemeComponent
   */
  @Transactional
  public ThemeComponent createNewTheme(User targetUser, String themeName) {
    ThemeComponent themeComponent = createNewTheme(targetUser);
    if (themeName != null && !themeName.isBlank()) {
      themeComponent.setThemeName(themeName);
    }
    return themeComponent;
  }

  /**
   * 소유자 또는 관리자의 권한을 확인한 뒤 테마와 관련 스타일·이미지 연결을 삭제한다.
   *
   * @param id 삭제할 테마 ID
   * @throws ResourceNotFoundException 테마가 존재하지 않는 경우
   * @throws AccessDeniedException 현재 사용자가 테마 소유자도 관리자도 아닌 경우
   */
  @Transactional
  public void deleteTheme(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    if (!ownerAdminPolicy.validate(themeComponent.getUser())) {
      throw new AccessDeniedException("failed to delete theme : invalid user or role");
    }
    // 먼저 관련된 테마 스타일과 테마 이미지를 삭제
    themeStyleRepository.deleteByThemeComponentId(id);
    themeImageRepository.deleteByThemeComponentId(id);
    // 그 다음 테마 컴포넌트를 삭제
    themeComponentRepository.delete(themeComponent);
  }

  @Transactional
  public ThemeComponentDto markAsDone(Integer id) {
    ThemeComponent themeComponent = themeComponentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Theme not found with id: " + id));
    themeComponent.setIsDone(true);
    themeComponentRepository.save(themeComponent);
    return themeComponentMapper.convertToDto(themeComponent);
  }
}
