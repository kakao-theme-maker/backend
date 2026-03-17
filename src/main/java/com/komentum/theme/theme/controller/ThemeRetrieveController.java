package com.komentum.theme.theme.controller;

import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeRetrieveController {

  private final ThemeRetrieveService themeRetrieveService;

  @GetMapping
  public ResponseEntity<List<ThemeComponentDto>> getAllThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(themeRetrieveService.getAllThemes(pageable));
  }

  @GetMapping("/public")
  public ResponseEntity<List<ThemeComponentDto>> getPublicThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(themeRetrieveService.getPublicThemes(pageable));
  }

  @GetMapping("/user/{userEmail}")
  public ResponseEntity<List<ThemeComponentDto>> getThemesByUserEmail(
      @PathVariable("userEmail") String userEmail,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(
        themeRetrieveService.getThemesByUserEmail(userEmail, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ThemeComponentDto> getThemeById(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeRetrieveService.getThemeById(id));
  }

  @GetMapping("/completed")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemes(
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemes(
        pageable);
    return ResponseEntity.ok(completedThemes);
  }

  @GetMapping("/completed/user/{userEmail}")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemesByUser(
      @PathVariable("userEmail") String userEmail,
      @PageableDefault(size = 20) @ParameterObject Pageable pageable) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemesByUser(
        userEmail, pageable);
    return ResponseEntity.ok(completedThemes);
  }
}
