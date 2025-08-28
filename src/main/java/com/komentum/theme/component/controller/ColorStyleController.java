package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.repository.ColorStyleRepository;
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

  private final ColorStyleRepository colorStyleRepository;

  @Autowired
  public ColorStyleController(ColorStyleRepository colorStyleRepository) {
    this.colorStyleRepository = colorStyleRepository;
  }

  @PostMapping
  public ResponseEntity<ColorStyle> createColorStyle(@RequestBody ColorStyle colorStyle) {
    ColorStyle savedColorStyle = colorStyleRepository.save(colorStyle);
    return ResponseEntity.ok(savedColorStyle);
  }

  @GetMapping
  public ResponseEntity<List<ColorStyle>> getAllColorStyles() {
    List<ColorStyle> colorStyles = colorStyleRepository.findAll();
    return ResponseEntity.ok(colorStyles);
  }

  @GetMapping("/{colorTypeId}")
  public ResponseEntity<ColorStyle> getColorStyleById(
      @PathVariable("colorTypeId") Integer colorTypeId) {
    return colorStyleRepository.findById(colorTypeId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{colorTypeId}")
  public ResponseEntity<Void> deleteColorStyle(@PathVariable("colorTypeId") Integer colorTypeId) {
    if (colorStyleRepository.existsById(colorTypeId)) {
      colorStyleRepository.deleteById(colorTypeId);
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{colorTypeId}")
  public ResponseEntity<ColorStyle> updateColorStyle(
      @PathVariable("colorTypeId") Integer colorTypeId, @RequestBody ColorStyle colorStyle) {
    return colorStyleRepository.findById(colorTypeId).map(
            existingColorStyle -> {
              // 필드값만 업데이트
              // 만약 json에 id 값을 추가하면, 바꿀 수 있도록 해야하나? id는 항상 같아야 한다고 생각...
              existingColorStyle.update(colorStyle);
              ColorStyle updatedColorStyle = colorStyleRepository.save(existingColorStyle);
              return ResponseEntity.ok(updatedColorStyle);
            })
        .orElse(ResponseEntity.notFound().build());
  }
}
