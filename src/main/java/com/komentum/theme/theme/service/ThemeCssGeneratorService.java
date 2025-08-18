package com.komentum.theme.theme.service;

import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemeStyleDto;
import com.komentum.theme.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeCssGeneratorService {

    private final ThemeService themeService;
    
    private static final String DEFAULT_CSS_FILE = "exampleCss.css";
    
    /**
     * 테마 ID로 CSS 파일 생성
     */
    public String generateThemeCss(Integer themeId) {
        try {
            // 테마 데이터 조회
            ThemeComponentDto theme = themeService.getThemeById(themeId);
            
            // 기본 CSS 템플릿 로드
            String defaultCss = loadDefaultCssTemplate();
            
            // 테마 데이터로 CSS 속성 치환
            String customizedCss = replaceCssProperties(defaultCss, theme);
            
            log.info("테마 CSS 생성 완료: themeId={}, themeName={}", themeId, theme.getThemeName());
            return customizedCss;
            
        } catch (Exception e) {
            log.error("테마 CSS 생성 실패: themeId={}", themeId, e);
            throw new RuntimeException("CSS 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 기본 CSS 템플릿 로드
     */
    private String loadDefaultCssTemplate() throws IOException {
        try {
            // 클래스패스에서 파일 로드 시도
            ClassPathResource resource = new ClassPathResource(DEFAULT_CSS_FILE);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 클래스패스에 없으면 프로젝트 루트에서 로드
            log.warn("클래스패스에서 CSS 파일을 찾을 수 없어 프로젝트 루트에서 로드 시도");
            return Files.readString(Paths.get(DEFAULT_CSS_FILE), StandardCharsets.UTF_8);
        }
    }
    
    /**
     * CSS 속성 치환
     */
    private String replaceCssProperties(String defaultCss, ThemeComponentDto theme) {
        String result = defaultCss;
        
        // 테마 스타일 데이터를 Map으로 변환
        Map<String, String> styleMap = createStyleMap(theme);
        
        // ManifestStyle 속성들 먼저 처리
        result = replaceManifestProperties(result, theme, styleMap);
        
        // 기타 스타일 속성들 처리
        result = replaceStyleProperties(result, styleMap);
        
        return result;
    }
    
    /**
     * 테마 스타일을 Map으로 변환
     */
    private Map<String, String> createStyleMap(ThemeComponentDto theme) {
        Map<String, String> styleMap = new HashMap<>();
        
        if (theme.getStyles() != null) {
            for (ThemeStyleDto style : theme.getStyles()) {
                // iOS 스타일명을 키로 사용
                if (style.getIosStyleName() != null) {
                    styleMap.put(style.getIosStyleName(), style.getColor());
                }
                // Android 스타일명도 키로 사용
                if (style.getAndroidStyleName() != null) {
                    styleMap.put(style.getAndroidStyleName(), style.getColor());
                }
                // 설명을 키로도 사용 (매칭 우선순위를 높이기 위해)
                if (style.getExplain() != null) {
                    styleMap.put(style.getExplain(), style.getColor());
                }
            }
        }
        
        return styleMap;
    }
    
    /**
     * ManifestStyle 속성 치환
     */
    private String replaceManifestProperties(String css, ThemeComponentDto theme, Map<String, String> styleMap) {
        String result = css;
        
        // 테마 이름 치환
        if (theme.getThemeName() != null) {
            result = result.replaceAll(
                "(-kakaotalk-theme-name:\\s*')[^']*(')", 
                "$1" + theme.getThemeName() + "$2"
            );
        }
        
        // 테마 버전 치환
        if (theme.getVersionName() != null) {
            result = result.replaceAll(
                "(-kakaotalk-theme-version:\\s*')[^']*(')", 
                "$1" + theme.getVersionName() + "$2"
            );
        }
        
        return result;
    }
    
    /**
     * 스타일 속성 치환
     */
    private String replaceStyleProperties(String css, Map<String, String> styleMap) {
        String result = css;
        
        for (Map.Entry<String, String> entry : styleMap.entrySet()) {
            String propertyName = entry.getKey();
            String colorValue = entry.getValue();
            
            // CSS에서 해당 속성 찾아서 치환
            // 패턴: property-name: #색상값; 또는 property-name: #색상값;
            Pattern colorPattern = Pattern.compile(
                "(" + Pattern.quote(propertyName) + "\\s*:\\s*)#[A-Fa-f0-9]{6}(\\s*;)",
                Pattern.CASE_INSENSITIVE
            );
            
            Matcher matcher = colorPattern.matcher(result);
            if (matcher.find()) {
                result = matcher.replaceAll("$1" + colorValue + "$2");
                log.debug("CSS 속성 치환: {} -> {}", propertyName, colorValue);
            }
            
            // background-color 등의 일반적인 CSS 속성들도 매칭
            Pattern generalPattern = Pattern.compile(
                "((?:background-color|color|border-color)\\s*:\\s*)#[A-Fa-f0-9]{6}(\\s*;)",
                Pattern.CASE_INSENSITIVE
            );
            
            // 특정 조건에서만 일반 속성 치환 (너무 광범위하게 치환되지 않도록)
            if (propertyName.contains("color") || propertyName.contains("Color")) {
                // 색상 관련 속성만 치환
                result = result.replaceAll(
                    "(background-color\\s*:\\s*)#[A-Fa-f0-9]{6}(\\s*;.*" + Pattern.quote(propertyName) + ")",
                    "$1" + colorValue + "$2"
                );
            }
        }
        
        return result;
    }
    
    /**
     * CSS를 파일로 저장
     */
    public void saveCssToFile(String css, String fileName) {
        try {
            String directory = "generated-css";
            Files.createDirectories(Paths.get(directory));
            
            String filePath = directory + "/" + fileName;
            Files.write(Paths.get(filePath), css.getBytes(StandardCharsets.UTF_8));
            
            log.info("CSS 파일 저장 완료: {}", filePath);
        } catch (IOException e) {
            log.error("CSS 파일 저장 실패: {}", fileName, e);
            throw new RuntimeException("CSS 파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 테마별 CSS 파일명 생성
     */
    public String generateCssFileName(ThemeComponentDto theme) {
        return String.format("%s_v%s_%d.css", 
            theme.getThemeName().replaceAll("[^a-zA-Z0-9]", "_"),
            theme.getVersionNumber(),
            theme.getThemeComponentId()
        );
    }
}