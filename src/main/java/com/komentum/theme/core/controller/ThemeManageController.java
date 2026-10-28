package com.komentum.theme.core.controller;

import com.komentum.global.dto.CustomUserDetails;
import com.komentum.theme.core.dto.ThemeCloneRequest;
import com.komentum.theme.core.dto.ThemeComponentDto;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.facade.ThemeManagementFacade;
import com.komentum.theme.core.service.ThemeManageService;
import com.komentum.theme.core.service.seeder.DefaultThemeSeeder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "Theme Management", description = "테마 관리 API")
public class ThemeManageController {

  private final ThemeManageService themeManageService;
  private final ThemeManagementFacade themeManagementFacade;
  private final DefaultThemeSeeder defaultThemeSeeder;

  @PostMapping
  @Operation(
      summary = "인증된 사용자가 새로운 테마를 생성한다",
      description = "기본 테마의 이미지 연결과 스타일을 복사한다. 기본 테마에 비활성 에셋이 하나라도 포함되면 "
          + "404를 반환하고 새 테마를 포함한 생성 결과 전체를 롤백한다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "기본 테마를 복사한 새 테마 생성 성공"),
      @ApiResponse(responseCode = "404", description = "기본 테마에 비활성 에셋이 포함됨")
  })
  public ResponseEntity<ThemeDetailResponse> createNewTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return new ResponseEntity<>(
        themeManagementFacade.createThemeFromDefault(userDetails.getPublicUserId()),
        HttpStatus.CREATED
    );
  }

  @PutMapping("/{themeComponentId}")
  @Operation(
      summary = "인증된 사용자가 ID로 특정 테마를 수정한다",
      description = """
          완성 여부와 관계없이 동일 TypeCode에 연결된 동일 에셋 ID를 유지하면 비활성 에셋도 유지하거나 inset을 수정할 수 있다.
          다른 에셋으로 교체할 때는 활성 에셋만 허용한다. 다른 TypeCode에서 사용 중인 비활성 에셋의 재사용과 제거한 비활성 에셋의 재연결은 404로 실패한다.
          typeCodes에서 생략한 항목은 유지하고 값이 null인 항목은 이미지 연결만 제거한다. 에셋과 원본 파일은 삭제하지 않는다.
          에셋 검증에 실패하면 이름·색상·이미지 연결 등 전체 수정을 롤백한다.
          """)
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "테마 수정 성공", content = @Content),
      @ApiResponse(responseCode = "404", description = "요청한 에셋이 없거나 기존 연결 유지 대상 외의 에셋이 비활성 상태")
  })
  public ResponseEntity<Void> updateTheme(
      @PathVariable @Parameter(description = "수정할 테마의 ID", example = "1") Integer themeComponentId,
      @Valid @RequestBody ThemeUpdateRequest request) {
    themeManagementFacade.updateTheme(themeComponentId, request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 삭제한다")
  public ResponseEntity<Void> deleteTheme(
      @Parameter(description = "삭제할 테마의 ID", example = "1")
      @PathVariable("id") Integer id) {
    themeManageService.deleteTheme(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/done")
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 완성 상태로 표시한다")
  public ResponseEntity<ThemeComponentDto> markThemeAsDone(
      @Parameter(description = "완성으로 표시할 테마의 ID", example = "1")
      @PathVariable("id") Integer id) {
    return ResponseEntity.ok(themeManageService.markAsDone(id));
  }

  @PostMapping("/default/seed")
  @Operation(summary = "root user가 디폴트 테마를 시딩한다")
  public ResponseEntity<Void> seedDefaultTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    defaultThemeSeeder.seedDefaultThemes(userDetails.getPublicUserId());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/clone")
  @Operation(
      summary = "인증된 사용자가 요청 본문의 이미지/색상 정보를 기반으로 테마를 복제한다",
      description = "요청 데이터로 새 이미지 연결을 생성하므로 기존 테마에서 사용 중인 에셋도 활성 상태여야 한다. "
          + "요청에 비활성 에셋이 하나라도 포함되면 404를 반환하고 새 테마를 포함한 복제 결과 전체를 롤백한다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "테마 복제 성공"),
      @ApiResponse(responseCode = "404", description = "요청한 에셋이나 타입이 없거나 에셋이 비활성 상태")
  })
  public ResponseEntity<ThemeDetailResponse> cloneTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody ThemeCloneRequest request
  ) {
    return new ResponseEntity<>(
        themeManagementFacade.cloneTheme(request, userDetails.getPublicUserId()),
        HttpStatus.CREATED
    );
  }
}
