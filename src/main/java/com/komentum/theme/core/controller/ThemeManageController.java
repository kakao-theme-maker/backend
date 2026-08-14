package com.komentum.theme.core.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.theme.core.dto.ThemeComponentDto;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.facade.ThemeManagementFacade;
import com.komentum.theme.core.service.ThemeManageService;
import com.komentum.theme.core.service.seeder.DefaultThemeSeeder;
import com.komentum.theme.ios.dto.IosThemePackageResponse;
import com.komentum.theme.ios.editor.IosThemeMaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "Theme Management", description = "테마 관리 API")
public class ThemeManageController {

  private final ThemeManageService themeManageService;
  private final ThemeManagementFacade themeManagementFacade;
  private final IosThemeMaker iosThemeMaker;
  private final DefaultThemeSeeder defaultThemeSeeder;

  @PostMapping
  @Operation(summary = "인증된 사용자가 새로운 테마를 생성한다")
  public ResponseEntity<ThemeDetailResponse> createNewTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return new ResponseEntity<>(
        themeManagementFacade.createThemeFromDefault(userDetails.getPublicUserId()),
        HttpStatus.CREATED
    );
  }

  @PutMapping("/{themeComponentId}")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 수정한다")
  public ResponseEntity<Void> updateTheme(
      @PathVariable @Parameter(description = "수정할 테마의 ID", example = "1") Integer themeComponentId,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody ThemeUpdateRequest request) {
    themeManagementFacade.updateTheme(themeComponentId, request, userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{themeComponentId}/packages/ios")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마의 iOS ktheme 패키지를 생성한다")
  public ResponseEntity<IosThemePackageResponse> makeIosThemePackage(
      @PathVariable @Parameter(description = "패키징할 테마의 ID", example = "1") Integer themeComponentId) {
    return ResponseEntity.ok(iosThemeMaker.makeTheme(themeComponentId));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 삭제한다")
  public ResponseEntity<Void> deleteTheme(
      @Parameter(description = "삭제할 테마의 ID", example = "1")
      @PathVariable("id") Integer id) {
    themeManageService.deleteTheme(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/done")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 완성 상태로 표시한다")
  public ResponseEntity<ThemeComponentDto> markThemeAsDone(
      @Parameter(description = "완성으로 표시할 테마의 ID", example = "1")
      @PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeManageService.markAsDone(id));
  }

  @PostMapping("/default/seed")
  @Operation(summary = "root user가 디폴트 테마를 시딩한다")
  public ResponseEntity<Void> seedDefaultTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    defaultThemeSeeder.seedDefaultThemes(userDetails.getPublicUserId());
    return ResponseEntity.ok().build();
  }
}
