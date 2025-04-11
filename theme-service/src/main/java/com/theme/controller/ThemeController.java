package com.theme.controller;

import com.theme.dto.CreateThemeRequest;
import com.theme.dto.ThemeComponentDto;
import com.theme.service.ThemeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;

    @Autowired
    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public ResponseEntity<List<ThemeComponentDto>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    @GetMapping("/public")
    public ResponseEntity<List<ThemeComponentDto>> getPublicThemes() {
        return ResponseEntity.ok(themeService.getPublicThemes());
    }

    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<ThemeComponentDto>> getThemesByUserEmail(@PathVariable("userEmail") String userEmail) {
        return ResponseEntity.ok(themeService.getThemesByUserEmail(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThemeComponentDto> getThemeById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(themeService.getThemeById(id));
    }

    @PostMapping
    public ResponseEntity<ThemeComponentDto> createTheme(@Valid @RequestBody CreateThemeRequest request) {
        return new ResponseEntity<>(themeService.createTheme(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThemeComponentDto> updateTheme(@PathVariable("id") Integer id, @Valid @RequestBody CreateThemeRequest request) {
        return ResponseEntity.ok(themeService.updateTheme(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable("id") Integer id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/done")
    public ResponseEntity<ThemeComponentDto> markThemeAsDone(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(themeService.markAsDone(id));
    }

    @GetMapping("/completed")
    public ResponseEntity<List<ThemeComponentDto>> getCompletedThemes() {
        List<ThemeComponentDto> completedThemes = themeService.getCompletedThemes();
        return ResponseEntity.ok(completedThemes);
    }

    @GetMapping("/completed/user/{userEmail}")
    public ResponseEntity<List<ThemeComponentDto>> getCompletedThemesByUser(@PathVariable("userEmail") String userEmail) {
        List<ThemeComponentDto> completedThemes = themeService.getCompletedThemesByUser(userEmail);
        return ResponseEntity.ok(completedThemes);
    }

}
