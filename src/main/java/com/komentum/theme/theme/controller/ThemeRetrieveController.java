package com.komentum.theme.theme.controller;

import com.komentum.global.dto.PageableRequestDto;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(themeRetrieveService.getAllThemes(pageableRequestDto.toPageable()));
  }

  @GetMapping("/public")
  public ResponseEntity<List<ThemeComponentDto>> getPublicThemes(
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(themeRetrieveService.getPublicThemes(pageableRequestDto.toPageable()));
  }

  @GetMapping("/user/{userEmail}")
  public ResponseEntity<List<ThemeComponentDto>> getThemesByUserEmail(
      @PathVariable("userEmail") String userEmail,
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    return ResponseEntity.ok(
        themeRetrieveService.getThemesByUserEmail(userEmail, pageableRequestDto.toPageable()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ThemeComponentDto> getThemeById(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeRetrieveService.getThemeById(id));
  }

  @GetMapping("/completed")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemes(
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemes(
        pageableRequestDto.toPageable());
    return ResponseEntity.ok(completedThemes);
  }

  @GetMapping("/completed/user/{userEmail}")
  public ResponseEntity<List<ThemeComponentDto>> getCompletedThemesByUser(
      @PathVariable("userEmail") String userEmail,
      @Valid @ModelAttribute PageableRequestDto pageableRequestDto) {
    List<ThemeComponentDto> completedThemes = themeRetrieveService.getCompletedThemesByUser(
        userEmail, pageableRequestDto.toPageable());
    return ResponseEntity.ok(completedThemes);
  }
}
