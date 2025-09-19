package com.komentum.theme.component.controller;

import com.komentum.theme.component.dto.ColorStyleResponse;
import com.komentum.theme.component.dto.CreateColorStyleRequest;
import com.komentum.theme.component.dto.UpdateColorStyleRequest;
import com.komentum.theme.component.service.ColorStyleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/color-styles")
public class ColorStyleController {

  private final ColorStyleService colorStyleService;

  public ColorStyleController(ColorStyleService colorStyleService) {
    this.colorStyleService = colorStyleService;
  }

  @PostMapping
  public ResponseEntity<ColorStyleResponse> createColorStyle(@Valid @RequestBody CreateColorStyleRequest request) {
    var createdColorStyle = colorStyleService.createColorStyle(request);
    return ResponseEntity.ok(ColorStyleResponse.from(createdColorStyle));
  }

  @GetMapping
  public ResponseEntity<List<ColorStyleResponse>> getAllColorStyles() {
    var colorStyles = colorStyleService.getAllColorStyles()
        .stream()
        .map(ColorStyleResponse::from)
        .collect(Collectors.toList());
    return ResponseEntity.ok(colorStyles);
  }

  @GetMapping("/{colorStyleId}")
  public ResponseEntity<ColorStyleResponse> getColorStyleById(
      @PathVariable("colorStyleId") Integer colorStyleId) {
    var colorStyle = colorStyleService.getColorStyleById(colorStyleId);
    return ResponseEntity.ok(ColorStyleResponse.from(colorStyle));
  }

  @DeleteMapping("/{colorStyleId}")
  public ResponseEntity<Void> deleteColorStyle(@PathVariable("colorStyleId") Integer colorStyleId) {
    colorStyleService.deleteColorStyle(colorStyleId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{colorStyleId}")
  public ResponseEntity<ColorStyleResponse> updateColorStyle(
      @PathVariable("colorStyleId") Integer colorStyleId, @Valid @RequestBody UpdateColorStyleRequest request) {
    var updatedColorStyle = colorStyleService.updateColorStyle(colorStyleId, request);
    return ResponseEntity.ok(ColorStyleResponse.from(updatedColorStyle));
  }
}
