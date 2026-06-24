package com.komentum.designcomponent.controller;

import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.dto.ColorStyleCreateDto;
import com.komentum.designcomponent.dto.ColorStyleResponse;
import com.komentum.designcomponent.dto.ColorStyleUpdateRequest;
import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.mapper.ColorStyleMapper;
import com.komentum.designcomponent.service.ColorStyleSeeder;
import com.komentum.designcomponent.service.ColorStyleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/color-styles")
@RequiredArgsConstructor
public class ColorStyleController {

  private final ColorStyleService colorStyleService;
  private final ColorStyleSeeder colorStyleSeeder;
  private final ColorStyleMapper colorStyleMapper;

  @PostMapping
  @Operation(summary = "Admin 사용자가 새로운 color style을 생성한다")
  public ResponseEntity<ColorStyleResponse> createColorStyle(
      @Valid @RequestBody ColorStyleCreateDto request) {
    ColorStyle createdColorStyle = colorStyleService.createColorStyle(request);
    return ResponseEntity.ok(colorStyleMapper.toColorStyleResponse(createdColorStyle));
  }

  @GetMapping
  @Operation(summary = "인증된 사용자가 모든 color style을 조회한다")
  public ResponseEntity<List<ColorStyleResponse>> getAllColorStyles() {
    List<ColorStyleResponse> colorStyles = colorStyleService.getAllColorStyles()
        .stream()
        .map(colorStyleMapper::toColorStyleResponse)
        .collect(Collectors.toList());
    return ResponseEntity.ok(colorStyles);
  }

  @GetMapping("/{color_style_id}")
  @Operation(summary = "인증된 ID=color_style_id인 colorStyle을 조회한다")
  public ResponseEntity<ColorStyleResponse> getColorStyleById(
      @PathVariable("color_style_id") Integer colorStyleId) {
    ColorStyle colorStyle = colorStyleService.getColorStyleById(colorStyleId);
    return ResponseEntity.ok(colorStyleMapper.toColorStyleResponse(colorStyle));
  }

  @PutMapping("/{color_style_id}")
  @Operation(summary = "Admin 사용자가 ID=color_style_id인 color style을 수정한다")
  public ResponseEntity<ColorStyleResponse> updateColorStyle(
      @PathVariable("color_style_id") Integer colorStyleId,
      @Valid @RequestBody ColorStyleUpdateRequest request) {
    ColorStyle updatedColorStyle = colorStyleService.updateColorStyle(colorStyleId, request);
    return ResponseEntity.ok(colorStyleMapper.toColorStyleResponse(updatedColorStyle));
  }

  @PutMapping("/seed")
  @Operation(summary = "Admin 사용자가 시드 데이터를 기반으로 color style 정보를 수정/삽입한다")
  public ResponseEntity<SeedResult> upsertColorStyleBySeed() {
    SeedResult result = colorStyleSeeder.upsertColorStyleSeed();
    return ResponseEntity.ok(result);
  }
}
