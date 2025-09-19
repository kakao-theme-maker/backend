package com.komentum.theme.theme.controller;

import com.komentum.theme.component.dto.CreateThemeRequest;
import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeManageService;
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
public class ThemeManageController {

  private final ThemeManageService themeManageService;

  @PostMapping
  public ResponseEntity<ThemeComponentDto> createTheme(
      @Valid @RequestBody CreateThemeRequest request) {
    return new ResponseEntity<>(themeManageService.createTheme(request), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ThemeComponentDto> updateTheme(@PathVariable("id") Integer id,
      @Valid @RequestBody CreateThemeRequest request) {
    return ResponseEntity.ok(themeManageService.updateTheme(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTheme(@PathVariable("id") Integer id) {
    themeManageService.deleteTheme(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/done")
  public ResponseEntity<ThemeComponentDto> markThemeAsDone(@PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeManageService.markAsDone(id));
  }
}
