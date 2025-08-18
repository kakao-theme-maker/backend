package com.komentum.theme.theme.controller;

import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.service.ThemeCssGeneratorService;
import com.komentum.theme.theme.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
@Tag(name = "Theme CSS", description = "테마 CSS 생성 API")
public class ThemeCssController {

    private final ThemeCssGeneratorService themeCssGeneratorService;
    private final ThemeService themeService;

    /**
     * 테마 CSS 생성 및 반환
     */
    @GetMapping("/{themeId}/css")
    @Operation(summary = "테마 CSS 생성", description = "테마 ID로 커스텀 CSS 파일을 생성하여 반환합니다")
    public ResponseEntity<String> generateThemeCss(
            @Parameter(description = "테마 ID", required = true)
            @PathVariable Integer themeId) {
        
        try {
            log.info("테마 CSS 생성 요청: themeId={}", themeId);
            
            // CSS 생성
            String css = themeCssGeneratorService.generateThemeCss(themeId);
            
            // 테마 정보 조회하여 파일명 생성
            ThemeComponentDto theme = themeService.getThemeById(themeId);
            String fileName = themeCssGeneratorService.generateCssFileName(theme);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", fileName);
            
            log.info("테마 CSS 생성 완료: themeId={}, fileName={}", themeId, fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(css);
                    
        } catch (Exception e) {
            log.error("테마 CSS 생성 실패: themeId={}", themeId, e);
            return ResponseEntity.internalServerError()
                    .body("CSS 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 테마 CSS 생성 및 파일 저장
     */
    @PostMapping("/{themeId}/css/save")
    @Operation(summary = "테마 CSS 파일 저장", description = "테마 CSS를 생성하여 서버에 파일로 저장합니다")
    public ResponseEntity<Map<String, Object>> saveThemeCss(
            @Parameter(description = "테마 ID", required = true)
            @PathVariable Integer themeId) {
        
        try {
            log.info("테마 CSS 파일 저장 요청: themeId={}", themeId);
            
            // CSS 생성
            String css = themeCssGeneratorService.generateThemeCss(themeId);
            
            // 테마 정보 조회하여 파일명 생성
            ThemeComponentDto theme = themeService.getThemeById(themeId);
            String fileName = themeCssGeneratorService.generateCssFileName(theme);
            
            // 파일 저장
            themeCssGeneratorService.saveCssToFile(css, fileName);
            
            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CSS 파일이 성공적으로 저장되었습니다.");
            response.put("fileName", fileName);
            response.put("themeId", themeId);
            response.put("themeName", theme.getThemeName());
            response.put("versionName", theme.getVersionName());
            
            log.info("테마 CSS 파일 저장 완료: themeId={}, fileName={}", themeId, fileName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("테마 CSS 파일 저장 실패: themeId={}", themeId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "CSS 파일 저장 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 테마 CSS 미리보기 (HTML 형태)
     */
    @GetMapping("/{themeId}/css/preview")
    @Operation(summary = "테마 CSS 미리보기", description = "생성될 CSS를 HTML로 미리볼 수 있습니다")
    public ResponseEntity<String> previewThemeCss(
            @Parameter(description = "테마 ID", required = true)
            @PathVariable Integer themeId) {
        
        try {
            log.info("테마 CSS 미리보기 요청: themeId={}", themeId);
            
            // CSS 생성
            String css = themeCssGeneratorService.generateThemeCss(themeId);
            
            // 테마 정보 조회
            ThemeComponentDto theme = themeService.getThemeById(themeId);
            
            // HTML로 감싸서 미리보기 제공
            String html = generatePreviewHtml(theme, css);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(html);
                    
        } catch (Exception e) {
            log.error("테마 CSS 미리보기 실패: themeId={}", themeId, e);
            return ResponseEntity.internalServerError()
                    .body("<html><body><h1>오류</h1><p>CSS 미리보기 중 오류가 발생했습니다: " 
                          + e.getMessage() + "</p></body></html>");
        }
    }
    
    /**
     * 미리보기용 HTML 생성
     */
    private String generatePreviewHtml(ThemeComponentDto theme, String css) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>테마 CSS 미리보기 - %s</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .header { background-color: #f5f5f5; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
                        .css-content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; white-space: pre-wrap; font-family: monospace; }
                        .info { margin-bottom: 10px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>테마 CSS 미리보기</h1>
                        <div class="info"><strong>테마명:</strong> %s</div>
                        <div class="info"><strong>버전:</strong> %s</div>
                        <div class="info"><strong>사용자:</strong> %s</div>
                        <div class="info"><strong>공개여부:</strong> %s</div>
                    </div>
                    <h2>생성된 CSS:</h2>
                    <div class="css-content">%s</div>
                </body>
                </html>
                """.formatted(
                theme.getThemeName(),
                theme.getThemeName(),
                theme.getVersionName(),
                theme.getUserEmail(),
                theme.getIsPublic() ? "공개" : "비공개",
                css.replace("<", "&lt;").replace(">", "&gt;")
        );
    }
}