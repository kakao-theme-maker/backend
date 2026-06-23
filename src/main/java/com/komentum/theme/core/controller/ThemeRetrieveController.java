package com.komentum.theme.core.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.theme.core.dto.ThemeComponentDto;
import com.komentum.theme.core.dto.ThemePreviewDto;
import com.komentum.theme.core.service.ThemeRetrieveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "Theme Retrieve", description = "테마 조회 API")
public class ThemeRetrieveController {

  private final ThemeRetrieveService themeRetrieveService;

  @GetMapping
  @Operation(summary = "인증된 사용자가 모든 테마를 조회한다")
  public ResponseEntity<List<ThemeComponentDto>> getAllThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(themeRetrieveService.getAllThemes(pageable));
  }

  @GetMapping("/public")
  @Operation(summary = "인증된 사용자가 공개된 모든 테마를 조회한다")
  public ResponseEntity<List<ThemeComponentDto>> getPublicThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(themeRetrieveService.getPublicThemes(pageable));
  }

  @GetMapping("/user/{userEmail}")
  @Operation(summary = "인증된 사용자가 특정 사용자의 모든 테마를 조회한다")
  public ResponseEntity<List<ThemeComponentDto>> getThemesByUserEmail(
      @Parameter(description = "조회할 사용자의 이메일", example = "user@example.com")
      @PathVariable("userEmail") String userEmail,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(
        themeRetrieveService.getThemesByUserEmail(userEmail, pageable));
  }

  @GetMapping("/{id}")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 조회한다")
  public ResponseEntity<ThemeComponentDto> getThemeById(
      @Parameter(description = "조회할 테마의 ID", example = "1")
      @PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeRetrieveService.getThemeById(id));
  }

  @GetMapping("/completed")
  @Operation(summary = "인증된 사용자가 완성된 모든 테마를 조회한다")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemes(
        pageable);
    return ResponseEntity.ok(completedThemes);
  }

  @GetMapping("/completed/user/{userEmail}")
  @Operation(summary = "인증된 사용자가 특정 사용자의 완성된 테마를 조회한다")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemesByUser(
      @Parameter(description = "조회할 사용자의 이메일", example = "user@example.com")
      @PathVariable("userEmail") String userEmail,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemesByUser(
        userEmail, pageable);
    return ResponseEntity.ok(completedThemes);
  }

  @GetMapping("/popular")
  @Operation(summary = "인증된 사용자가 인기 테마 목록을 조회한다")
  public ResponseEntity<List<ThemePreviewDto>> findPopularThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<ThemePreviewDto> res = themeRetrieveService.findPopularThemeList(pageable,
        userDetails.getUsername());
    return ResponseEntity.ok(res);
  }

  @GetMapping("/bookmarked")
  @Operation(summary = "인증된 사용자가 현재 북마크한 테마 목록을 조회한다")
  public ResponseEntity<List<ThemePreviewDto>> findBookmarkedThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    List<ThemePreviewDto> res = themeRetrieveService.findBookmarkedThemeList(pageable,
        userDetails.getUsername());
    return ResponseEntity.ok(res);
  }
}
