package com.komentum.theme.theme.controller;

import com.komentum.theme.theme.dto.CreateThemeRequest;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  @PostMapping
  @Operation(summary = "인증된 사용자가 새로운 테마를 생성한다")
  public ResponseEntity<ThemeComponentDto> createTheme(
      @Valid @RequestBody CreateThemeRequest request) {
    return new ResponseEntity<>(themeManageService.createTheme(request), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 수정한다")
  public ResponseEntity<ThemeComponentDto> updateTheme(
      @Parameter(description = "수정할 테마의 ID", example = "1")
      @PathVariable("id") Integer id,
      @Valid @RequestBody CreateThemeRequest request) {
    return ResponseEntity.ok(themeManageService.updateTheme(id, request));
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
}
