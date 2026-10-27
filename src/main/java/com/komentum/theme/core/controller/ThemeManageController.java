package com.komentum.theme.core.controller;

import com.komentum.designcomponent.enums.TypeCode;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "Theme Management", description = "테마 관리 API")
public class ThemeManageController {

  // 이미지 외 수정 정보가 담긴 JSON part 이름
  private static final String REQUEST_PART_NAME = "request";

  private final ThemeManageService themeManageService;
  private final ThemeManagementFacade themeManagementFacade;
  private final DefaultThemeSeeder defaultThemeSeeder;

  @PostMapping
  @Operation(summary = "인증된 사용자가 새로운 테마를 생성한다")
  public ResponseEntity<ThemeDetailResponse> createNewTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return new ResponseEntity<>(
        themeManagementFacade.createThemeFromDefault(userDetails.getPublicUserId()),
        HttpStatus.CREATED
    );
  }

  @PutMapping(value = "/{themeComponentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "인증된 사용자가 ID로 특정 테마를 수정한다")
  public ResponseEntity<Void> updateTheme(
      @PathVariable @Parameter(description = "수정할 테마의 ID", example = "1") Integer themeComponentId,
      @RequestPart(REQUEST_PART_NAME) @Valid ThemeUpdateRequest request,
      @RequestParam MultiValueMap<String, MultipartFile> files) {
    themeManagementFacade.updateTheme(themeComponentId, request, toImageFileMap(files, request));
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
  @Operation(summary = "인증된 사용자가 요청 본문의 이미지/색상 정보를 기반으로 테마를 복제한다")
  public ResponseEntity<ThemeDetailResponse> cloneTheme(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody ThemeCloneRequest request
  ) {
    return new ResponseEntity<>(
        themeManagementFacade.cloneTheme(request, userDetails.getPublicUserId()),
        HttpStatus.CREATED
    );
  }

  /**
   * part 이름(TypeCode 값)별 파일 맵을 TypeCode별 이미지 파일 맵으로 변환한다. 파일 part의 key는 Jackson 역직렬화를 거치지 않아
   * TypeCode로 자동 변환되지 않으므로 직접 변환한다. Blob으로 전송된 request part는 파일로 분류되므로 제외한다.
   *
   * @param files   part 이름별 파일 맵
   * @param request 제거할 TypeCode 목록이 담긴 수정 요청
   * @return TypeCode별 교체할 이미지 파일 맵
   * @throws ResponseStatusException TypeCode에 없는 이름(UNKNOWN_TYPE_CODE), 같은 TypeCode의 파일이 2개
   *                                 이상(DUPLICATED_TYPE_CODE), 빈 파일(EMPTY_FILE), 파일과 deleteTypeCodes에
   *                                 같은 TypeCode가 존재(TYPE_CODE_CONFLICT)하는 경우 (400 Bad Request)
   */
  private Map<TypeCode, MultipartFile> toImageFileMap(MultiValueMap<String, MultipartFile> files,
      ThemeUpdateRequest request) {
    Map<TypeCode, MultipartFile> imageFiles = new EnumMap<>(TypeCode.class);
    files.forEach((partName, partFiles) -> {
      TypeCode typeCode;
      // request JSON part는 Blob(filename="blob")이라 파일 맵에도 포함되므로 이미지 처리에서 제외한다
      if (REQUEST_PART_NAME.equals(partName)) {
        return;
      }
      try {
        typeCode = TypeCode.from(partName);
      } catch (IllegalArgumentException e) {
        throw badRequest("UNKNOWN_TYPE_CODE", partName);
      }
      if (partFiles.size() > 1) {
        throw badRequest("DUPLICATED_TYPE_CODE", partName);
      }
      if (partFiles.get(0).isEmpty()) {
        throw badRequest("EMPTY_FILE", partName);
      }
      if (request.getDeleteTypeCodes() != null && request.getDeleteTypeCodes().contains(typeCode)) {
        throw badRequest("TYPE_CODE_CONFLICT", partName);
      }
      imageFiles.put(typeCode, partFiles.get(0));
    });
    return imageFiles;
  }

  private ResponseStatusException badRequest(String errorCode, String partName) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
        errorCode + ": invalid image part : " + partName);
  }
}
