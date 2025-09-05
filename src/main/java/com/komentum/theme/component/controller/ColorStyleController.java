package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.service.ColorStyleService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  public ColorStyleController(ColorStyleService colorStyleService) {
    this.colorStyleService = colorStyleService;
  }

  @PostMapping
  public ResponseEntity<ColorStyle> createColorStyle(@RequestBody ColorStyle colorStyle) {
    return ResponseEntity.ok(colorStyleService.createColorStyle(colorStyle));
  }

  @GetMapping
  public ResponseEntity<List<ColorStyle>> getAllColorStyles() {
    return ResponseEntity.ok(colorStyleService.getAllColorStyles());
  }

  @GetMapping("/{colorTypeId}")
  public ResponseEntity<ColorStyle> getColorStyleById(
      @PathVariable("colorTypeId") Integer colorTypeId) {
    return ResponseEntity.ok(colorStyleService.getColorStyleById(colorTypeId));
  }

  @DeleteMapping("/{colorTypeId}")
  public ResponseEntity<Void> deleteColorStyle(@PathVariable("colorTypeId") Integer colorTypeId) {
    colorStyleService.deleteColorStyle(colorTypeId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{colorTypeId}")
  public ResponseEntity<ColorStyle> updateColorStyle(
      @PathVariable("colorTypeId") Integer colorTypeId, @RequestBody ColorStyle colorStyle) {
    return ResponseEntity.ok(colorStyleService.updateColorStyle(colorTypeId, colorStyle));
  }
}
