package com.komentum.theme.theme.service;

import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemeStyleDto;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.ios.service.IosCssOptionService;
import com.komentum.theme.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeCssGeneratorService {

    private final ThemeRetrieveService themeRetrieveService;
    private final IosCssOptionService iosCssOptionService;
    
    private static final String DEFAULT_CSS_FILE = "exampleCss.css";
    
    /**
     * 테마 ID로 CSS 파일 생성
     */
    public String generateThemeCss(Integer themeId) {
        try {
            // 테마 데이터 조회
            ThemeComponentDto theme = themeRetrieveService.getThemeById(themeId);
            
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
                // ColorStyleId를 키로 사용하고, 색상값을 값으로 사용
                if (style.getColorStyleId() != null && style.getColor() != null) {
                    styleMap.put(style.getColorStyleId().toString(), style.getColor());
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
        
        // 테마 ID 생성 및 치환
        String themeId = "com.komentum.theme." + 
            theme.getThemeName().toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        result = result.replaceAll(
            "(-kakaotalk-theme-id:\\s*')[^']*(')", 
            "$1" + themeId + "$2"
        );
        
        // 작성자 치환
        if (theme.getUserEmail() != null) {
            result = result.replaceAll(
                "(-kakaotalk-author-name:\\s*')[^']*(')", 
                "$1" + theme.getUserEmail() + "$2"
            );
        }
        
        return result;
    }
    
    /**
     * 스타일 속성 치환 (개선된 버전)
     */
    private String replaceStyleProperties(String css, Map<String, String> styleMap) {
        String result = css;
        
        // iOS ColorStyle 정보 가져오기
        List<ColorStyle> iosColorStyles = iosCssOptionService.getAllIosCssOptions();
        
        for (Map.Entry<String, String> entry : styleMap.entrySet()) {
            String colorStyleId = entry.getKey();
            String colorValue = entry.getValue();
            
            // ColorStyle ID로 해당 ColorStyle 찾기
            ColorStyle colorStyle = findColorStyleById(iosColorStyles, colorStyleId);
            if (colorStyle == null) continue;
            
            // CSS 셀렉터와 속성명 조합으로 치환
            String selector = colorStyle.getStyleSheetPath();
            String property = colorStyle.getStyleElementName();
            
            // 특정 셀렉터 블록 내에서 속성 치환
            result = replacePropertyInSelector(result, selector, property, colorValue);
        }
        
        return result;
    }
    
    /**
     * 특정 셀렉터 블록 내에서 속성 치환
     */
    private String replacePropertyInSelector(String css, String selector, String property, String value) {
        // 셀렉터 블록 찾기 패턴
        Pattern selectorPattern = Pattern.compile(
            "(" + Pattern.quote(selector) + "\\s*\\{[^}]*?" + 
            Pattern.quote(property) + "\\s*:\\s*)[^;]+?(\\s*;[^}]*?\\})",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = selectorPattern.matcher(css);
        if (matcher.find()) {
            String replacement = matcher.replaceAll("$1" + value + "$2");
            log.debug("CSS 속성 치환: {} > {} -> {}", selector, property, value);
            return replacement;
        }
        
        return css;
    }
    
    /**
     * ColorStyle ID로 ColorStyle 찾기
     */
    private ColorStyle findColorStyleById(List<ColorStyle> colorStyles, String colorStyleId) {
        try {
            Integer id = Integer.parseInt(colorStyleId);
            return colorStyles.stream()
                .filter(cs -> cs.getColorStyleId().equals(id))
                .findFirst()
                .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * CSS를 파일로 저장
     */
    public String saveCssToFile(String css, Integer themeId, String themeName) {
        try {
            String directory = "generated-css";
            Files.createDirectories(Paths.get(directory));
            
            String fileName = generateCssFileName(themeId, themeName);
            String filePath = directory + "/" + fileName;
            Files.write(Paths.get(filePath), css.getBytes(StandardCharsets.UTF_8));
            
            log.info("CSS 파일 저장 완료: {}", filePath);
            return fileName;
        } catch (IOException e) {
            log.error("CSS 파일 저장 실패: themeId={}", themeId, e);
            throw new RuntimeException("CSS 파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 테마별 CSS 파일명 생성
     */
    public String generateCssFileName(Integer themeId, String themeName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeName = themeName.replaceAll("[^a-zA-Z0-9가-힣]", "_");
        return String.format("theme_%d_%s_%s.css", themeId, safeName, timestamp);
    }
}