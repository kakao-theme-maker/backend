package com.komentum.designcomponent.controller;

import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.service.seeder.ColorStyleSeeder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("unused-api")
@RequestMapping("/api/color-styles")
@RequiredArgsConstructor
@Tag(name = "color-style-controller")
public class ColorStyleSeedController {

  private final ColorStyleSeeder colorStyleSeeder;

  /**
   * 시드 데이터를 기반으로 color style 정보를 수정하거나 삽입한다.
   *
   * @return 시드 데이터 반영 결과
   */
  @PutMapping("/seed")
  @Operation(summary = "Admin 사용자가 시드 데이터를 기반으로 color style 정보를 수정/삽입한다")
  public ResponseEntity<SeedResult> upsertColorStyleBySeed() {
    SeedResult result = colorStyleSeeder.upsertColorStyleSeed();
    return ResponseEntity.ok(result);
  }
}
