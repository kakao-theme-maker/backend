package com.komentum.theme.theme.controller;

import com.komentum.theme.theme.dto.CssCustomizationRequest;
import com.komentum.theme.theme.dto.CssCustomizationResponse;
import com.komentum.theme.theme.service.CssCustomizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/themes/css")
@RequiredArgsConstructor
@Slf4j
public class CssCustomizationController {

    private final CssCustomizationService cssCustomizationService;

    /**
     * 테마 CSS 커스터마이징 및 저장
     */
    @PostMapping("/customize")
    public ResponseEntity<CssCustomizationResponse> customizeTheme(
        @Valid @RequestBody CssCustomizationRequest request) {
        
        log.info("테마 {} CSS 커스터마이징 요청", request.getThemeId());
        
        try {
            CssCustomizationResponse response = cssCustomizationService.customizeTheme(request);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("CSS 커스터마이징 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CssCustomizationResponse(e.getMessage(), null, null));
        }
    }

    /**
     * 테마 CSS 미리보기 (저장하지 않음)
     */
    @PostMapping("/preview")
    public ResponseEntity<CssCustomizationResponse> previewCss(
        @Valid @RequestBody CssCustomizationRequest request) {
        
        log.info("테마 {} CSS 미리보기 요청", request.getThemeId());
        
        try {
            CssCustomizationResponse response = cssCustomizationService.previewCss(request);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("CSS 미리보기 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new CssCustomizationResponse(e.getMessage(), null, null));
        }
    }
}