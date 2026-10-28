package com.komentum.theme.build.controller;

import com.komentum.designcomponent.enums.Platform;
import com.komentum.theme.build.dto.ThemeBuildStartRequest;
import com.komentum.theme.build.dto.ThemeBuildStartResponse;
import com.komentum.theme.build.dto.ThemeBuildStatusResponse;
import com.komentum.theme.build.dto.ThemeDownloadResponse;
import com.komentum.theme.build.service.ThemeBuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ThemeBuildController {

  private final ThemeBuildService themeBuildService;

  @PostMapping("/themes/{themeComponentId}/builds")
  @Operation(
      summary = "테마 패키지 제작 작업을 시작한다",
      description = "저장된 이미지 연결을 통해 비활성 에셋도 Android/iOS 빌드에 사용한다. "
          + "202 응답은 제작 작업 접수를 의미하며 제작 완료 여부는 작업 상태 조회 API로 확인한다.")
  @ApiResponses({
      @ApiResponse(responseCode = "202", description = "테마 제작 작업 접수 성공"),
      @ApiResponse(responseCode = "404", description = "테마가 존재하지 않음")
  })
  public ResponseEntity<ThemeBuildStartResponse> startThemeBuild(
      @Parameter(description = "제작할 테마 ID", example = "1")
      @PathVariable Integer themeComponentId,
      @Valid @RequestBody ThemeBuildStartRequest request
  ) {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(themeBuildService.startBuild(themeComponentId, request.platform()));
  }

  @GetMapping("/theme-builds/{buildId}")
  @Operation(summary = "테마 패키지 제작 작업 상태를 조회한다")
  public ResponseEntity<ThemeBuildStatusResponse> findThemeBuild(
      @Parameter(description = "조회할 제작 작업 ID", example = "1")
      @PathVariable Long buildId
  ) {
    return ResponseEntity.ok(themeBuildService.findBuild(buildId));
  }

  @GetMapping("/themes/{themeComponentId}/download")
  @Operation(summary = "완료된 테마 패키지의 다운로드 URL을 조회한다")
  public ResponseEntity<ThemeDownloadResponse> getThemeDownloadUrl(
      @Parameter(description = "다운로드할 테마 ID", example = "1")
      @PathVariable Integer themeComponentId,
      @Parameter(description = "다운로드할 플랫폼", example = "ANDROID | IOS")
      @RequestParam Platform platform
  ) {
    return ResponseEntity.ok(themeBuildService.getDownloadUrl(themeComponentId, platform));
  }
}
