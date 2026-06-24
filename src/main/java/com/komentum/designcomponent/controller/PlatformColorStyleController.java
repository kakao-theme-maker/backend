package com.komentum.designcomponent.controller;

import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.service.seeder.PlatformColorStyleSeeder;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform-color-styles")
@RequiredArgsConstructor
public class PlatformColorStyleController {

  private final PlatformColorStyleSeeder platformColorStyleSeeder;

  @PutMapping("/seeds")
  @Operation(summary = "플랫폼별 스타일 정보를 갱신 / 생성한다")
  public ResponseEntity<SeedResult> seedData() {
    SeedResult result = platformColorStyleSeeder.upsertPlatformColorStyle();
    return ResponseEntity.ok(result);
  }
}
