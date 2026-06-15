package com.komentum.theme.component.controller;

import com.komentum.theme.component.dto.SeedResult;
import com.komentum.theme.component.service.seeder.PlatformComponentTypeSeeder;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform-component-types")
@RequiredArgsConstructor
public class PlatformComponentTypeController {

  private final PlatformComponentTypeSeeder seeder;

  @PutMapping("/seeds")
  @Operation(summary = "플랫폼별 이미지 정보를 갱신 / 생성한다")
  public ResponseEntity<SeedResult> seedData() {
    SeedResult result = seeder.upsertPlatformComponentType();
    return ResponseEntity.ok(result);
  }
}
