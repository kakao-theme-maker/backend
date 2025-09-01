package com.komentum.theme.theme.controller;

import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<List<ThemeComponentDto>> getAllThemes() {
    return ResponseEntity.ok(themeRetrieveService.getAllThemes());
  }

  @GetMapping("/public")
  public ResponseEntity<List<ThemeComponentDto>> getPublicThemes() {
    return ResponseEntity.ok(themeRetrieveService.getPublicThemes());
  }

  @GetMapping("/user/{userEmail}")
  public ResponseEntity<List<ThemeComponentDto>> getThemesByUserEmail(
      @PathVariable("userEmail") String userEmail) {
    return ResponseEntity.ok(themeRetrieveService.getThemesByUserEmail(userEmail));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ThemeComponentDto> getThemeById(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeRetrieveService.getThemeById(id));
  }

  @GetMapping("/completed")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemes() {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemes();
    return ResponseEntity.ok(completedThemes);
  }

  @GetMapping("/completed/user/{userEmail}")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemesByUser(
      @PathVariable("userEmail") String userEmail) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemesByUser(
        userEmail);
    return ResponseEntity.ok(completedThemes);
  }
}
