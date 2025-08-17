package com.komentum.theme.theme.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.service.ColorStyleService;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.theme.theme.dto.CssCustomizationRequest;
import com.komentum.theme.theme.dto.CssCustomizationResponse;
import com.komentum.theme.theme.repository.ThemeComponentRepository;
import com.komentum.theme.theme.repository.ThemeStyleRepository;
import com.komentum.theme.utils.S3FileManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CssCustomizationService {

    private final ThemeStyleRepository themeStyleRepository;
    private final ThemeComponentRepository themeComponentRepository;
    private final ColorStyleService colorStyleService;
    private final S3FileManager s3FileManager;
    
    private static final String THEME_BUCKET_NAME = "komentum-theme";

    @Transactional(readOnly = true)
    public CssCustomizationResponse previewCss(CssCustomizationRequest request) {
        try {
            log.info("CSS 미리보기 요청: themeId={}, customizations={}", 
                    request.getThemeId(), request.getCustomizations().size());

            StringBuilder cssBuilder = new StringBuilder();
            cssBuilder.append("/* Preview Theme CSS */\n");

            for (CssCustomizationRequest.CssCustomization customization : request.getCustomizations()) {
                // ColorStyle에서 CSS 정보 조회
                ColorStyle colorStyle = colorStyleService.getCssOptionById(customization.getColorTypeId());
                
                // iOS 스타일명에서 선택자와 속성 추출
                String[] parts = colorStyle.getIosStyleName().split("\\|");
                if (parts.length == 2) {
                    String selector = parts[0];
                    String property = parts[1];
                    
                    cssBuilder.append(selector).append(" {\n");
                    String cssValue = convertImageUrlsToS3(customization.getValue());
                    cssBuilder.append("  ").append(property).append(": ").append(cssValue).append(";\n");
                    cssBuilder.append("}\n\n");
                }
            }

            return new CssCustomizationResponse(
                    "CSS 미리보기가 완료되었습니다.",
                    cssBuilder.toString(),
                    null
            );

        } catch (Exception e) {
            log.error("CSS 미리보기 실패", e);
            throw new RuntimeException("CSS 미리보기에 실패했습니다: " + e.getMessage());
        }
    }

    @Transactional
    public CssCustomizationResponse customizeTheme(CssCustomizationRequest request) {
        try {
            log.info("테마 CSS 커스터마이징 요청: themeId={}", request.getThemeId());

            // 테마 존재 여부 확인
            if (!themeComponentRepository.existsById(request.getThemeId())) {
                throw new RuntimeException("테마를 찾을 수 없습니다.");
            }

            // CSS 생성
            String cssContent = generateCustomizedCss(request);

            // ThemeStyle 테이블에 커스터마이징 정보 저장
            saveCustomizationsToDb(request);

            // CSS 파일 저장
            String downloadUrl = saveCssToFile(cssContent, request.getThemeId(), request.getUserEmail());

            // ktheme 파일 생성
            String kthemeFilePath = generateKthemeFile(cssContent, request);

            return new CssCustomizationResponse(
                    "CSS 커스터마이징이 완료되었습니다.",
                    cssContent,
                    kthemeFilePath != null ? kthemeFilePath : downloadUrl
            );

        } catch (Exception e) {
            log.error("CSS 커스터마이징 실패", e);
            throw new RuntimeException("CSS 커스터마이징에 실패했습니다: " + e.getMessage());
        }
    }

    private String generateCustomizedCss(CssCustomizationRequest request) {
        StringBuilder cssBuilder = new StringBuilder();
        cssBuilder.append("/* Customized Theme CSS - Generated at ").append(LocalDateTime.now()).append(" */\n");
        cssBuilder.append("/* User: ").append(request.getUserEmail()).append(" */\n");
        cssBuilder.append("/* Theme ID: ").append(request.getThemeId()).append(" */\n\n");

        try {
            // 모든 ColorStyle 옵션 조회
            List<ColorStyle> allColorStyles = colorStyleService.getAllCssOptions();
            log.info("전체 CSS 옵션 개수: {}", allColorStyles.size());
            
            // 사용자 커스터마이징 데이터를 Map으로 변환 (빠른 조회를 위해)
            java.util.Map<Integer, String> customizationMap = new java.util.HashMap<>();
            if (request.getCustomizations() != null) {
                for (CssCustomizationRequest.CssCustomization customization : request.getCustomizations()) {
                    customizationMap.put(customization.getColorTypeId(), customization.getValue());
                }
            }
            
            // 모든 CSS 옵션에 대해 처리
            for (ColorStyle colorStyle : allColorStyles) {
                try {
                    String[] parts = colorStyle.getIosStyleName().split("\\|");
                    if (parts.length == 2) {
                        String selector = parts[0];
                        String property = parts[1];
                        
                        // 사용자가 커스터마이징한 값이 있으면 사용, 없으면 기본값 사용
                        String cssValue;
                        if (customizationMap.containsKey(colorStyle.getColorTypeId())) {
                            // 사용자 커스터마이징 값
                            cssValue = customizationMap.get(colorStyle.getColorTypeId());
                            cssBuilder.append("/* ").append(colorStyle.getExplain()).append(" (사용자 설정) */\n");
                        } else {
                            // 기본값 설정
                            cssValue = getDefaultValue(colorStyle);
                            cssBuilder.append("/* ").append(colorStyle.getExplain()).append(" (기본값) */\n");
                        }
                        
                        cssBuilder.append(selector).append(" {\n");
                        String finalCssValue = convertImageUrlsToS3(cssValue);
                        cssBuilder.append("  ").append(property).append(": ").append(finalCssValue).append(";\n");
                        cssBuilder.append("}\n\n");
                    }
                } catch (Exception e) {
                    log.warn("CSS 옵션 처리 실패: colorStyleId={}", colorStyle.getColorTypeId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("전체 CSS 생성 실패, 커스터마이징만 포함된 CSS로 fallback", e);
            
            // Fallback: 기존 방식으로 커스터마이징된 것만 포함
            for (CssCustomizationRequest.CssCustomization customization : request.getCustomizations()) {
                try {
                    ColorStyle colorStyle = colorStyleService.getCssOptionById(customization.getColorTypeId());
                    
                    String[] parts = colorStyle.getIosStyleName().split("\\|");
                    if (parts.length == 2) {
                        String selector = parts[0];
                        String property = parts[1];
                        
                        cssBuilder.append("/* ").append(colorStyle.getExplain()).append(" */\n");
                        cssBuilder.append(selector).append(" {\n");
                        String cssValue = convertImageUrlsToS3(customization.getValue());
                        cssBuilder.append("  ").append(property).append(": ").append(cssValue).append(";\n");
                        cssBuilder.append("}\n\n");
                    }
                } catch (Exception fallbackException) {
                    log.warn("Fallback CSS 옵션 처리 실패: colorTypeId={}", customization.getColorTypeId(), fallbackException);
                }
            }
        }

        return cssBuilder.toString();
    }
    
    /**
     * CSS 옵션의 기본값을 반환
     * 
     * @param colorStyle CSS 옵션
     * @return 기본값
     */
    private String getDefaultValue(ColorStyle colorStyle) {
        String property = colorStyle.getIosStyleName().split("\\|")[1];
        
        // 속성 타입에 따른 기본값 설정
        if (property.contains("color")) {
            return "#FFFFFF"; // 기본 색상: 흰색
        } else if (property.contains("alpha")) {
            return "1.0"; // 기본 투명도: 불투명
        } else if (property.contains("image")) {
            // 기본 이미지는 속성명에 따라 결정
            if (property.contains("home")) {
                return "home-icon.png";
            } else if (property.contains("friends")) {
                return "friends-icon.png";
            } else if (property.contains("chats")) {
                return "chat-icon.png";
            } else if (property.contains("bubble") || property.contains("background-image")) {
                if (property.contains("send") || colorStyle.getExplain().contains("보낸")) {
                    return "send-bubble.png";
                } else if (property.contains("receive") || colorStyle.getExplain().contains("받은")) {
                    return "receive-bubble.png";
                } else {
                    return "default-bg.png";
                }
            } else {
                return "default-icon.png";
            }
        } else if (property.contains("edgeinsets")) {
            return "10,10,10,10"; // 기본 여백
        } else if (property.contains("theme-name")) {
            return "Default Theme";
        } else if (property.contains("theme-version")) {
            return "1.0";
        } else if (property.contains("theme-url")) {
            return "https://example.com";
        } else if (property.contains("author-name")) {
            return "Theme Creator";
        } else if (property.contains("theme-id")) {
            return "default-theme";
        } else {
            return "#000000"; // 기타: 검은색
        }
    }

    private void saveCustomizationsToDb(CssCustomizationRequest request) {
        // 기존 커스터마이징 삭제 (덮어쓰기)
        themeStyleRepository.deleteByThemeComponentIdAndCssSelectorIsNotNull(request.getThemeId());

        for (CssCustomizationRequest.CssCustomization customization : request.getCustomizations()) {
            try {
                ColorStyle colorStyle = colorStyleService.getCssOptionById(customization.getColorTypeId());
                
                String[] parts = colorStyle.getIosStyleName().split("\\|");
                if (parts.length == 2) {
                    String selector = parts[0];
                    String property = parts[1];
                    
                    ThemeStyle themeStyle = ThemeStyle.builder()
                            .themeComponentId(request.getThemeId())
                            .colorTypeId(customization.getColorTypeId())
                            .cssSelector(selector)
                            .propertyName(property)
                            .color(customization.getValue())
                            .build();
                    
                    themeStyleRepository.save(themeStyle);
                    log.debug("ThemeStyle 저장: {}", themeStyle);
                }
            } catch (Exception e) {
                log.warn("ThemeStyle 저장 실패: colorTypeId={}", customization.getColorTypeId(), e);
            }
        }
    }

    private String saveCssToFile(String cssContent, Integer themeId, String userEmail) {
        try {
            // 파일명 생성: theme_{themeId}_{userEmail}_{timestamp}.css
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("theme_%d_%s_%s.css", 
                    themeId, 
                    userEmail.replace("@", "_").replace(".", "_"), 
                    timestamp);
            
            // 프로젝트 루트의 generated-css 디렉토리에 저장
            String projectRoot = System.getProperty("user.dir");
            String cssDir = projectRoot + "/generated-css/";
            
            // 디렉토리가 없으면 생성
            Files.createDirectories(Paths.get(cssDir));
            
            String outputPath = cssDir + fileName;
            Files.write(Paths.get(outputPath), cssContent.getBytes("UTF-8"));
            
            log.info("CSS 파일 저장 완료: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("CSS 파일 저장 실패", e);
            return null;
        }
    }

    /**
     * CSS 값에서 이미지 파일명을 S3 CloudFront URL로 변환
     * 
     * @param cssValue CSS 값 (예: "url(image.png)" 또는 "#FF0000")
     * @return S3 URL로 변환된 CSS 값
     */
    private String convertImageUrlsToS3(String cssValue) {
        if (cssValue == null || cssValue.trim().isEmpty()) {
            return cssValue;
        }

        try {
            // url() 패턴 매칭: url(filename.ext)
            if (cssValue.matches(".*url\\([^)]+\\).*")) {
                Pattern pattern = Pattern.compile("url\\(([^)]+)\\)");
                Matcher matcher = pattern.matcher(cssValue);
                
                StringBuffer result = new StringBuffer();
                while (matcher.find()) {
                    String fileName = matcher.group(1).trim();
                    
                    // 이미지 파일 확장자 체크
                    if (isImageFile(fileName)) {
                        // S3 CloudFront URL로 변환
                        String s3Url = s3FileManager.resolveFilePath("theme-assets/" + fileName);
                        matcher.appendReplacement(result, "url(" + s3Url + ")");
                    } else {
                        matcher.appendReplacement(result, matcher.group(0));
                    }
                }
                matcher.appendTail(result);
                return result.toString();
            }
            
            // 단순 이미지 파일명인 경우 (확장자만 있는 경우)
            if (isImageFile(cssValue)) {
                return s3FileManager.resolveFilePath("theme-assets/" + cssValue);
            }
            
            return cssValue; // 색상코드 등은 그대로 반환
            
        } catch (Exception e) {
            log.warn("이미지 URL 변환 실패: {}", cssValue, e);
            return cssValue; // 실패 시 원본 반환
        }
    }

    /**
     * 파일이 이미지 파일인지 확인
     * 
     * @param fileName 파일명
     * @return 이미지 파일 여부
     */
    private boolean isImageFile(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        String lowerCase = fileName.toLowerCase();
        return lowerCase.endsWith(".png") || 
               lowerCase.endsWith(".jpg") || 
               lowerCase.endsWith(".jpeg") || 
               lowerCase.endsWith(".svg") || 
               lowerCase.endsWith(".gif") || 
               lowerCase.endsWith(".webp");
    }

    /**
     * ktheme 파일 생성 (CSS + S3에서 다운로드한 이미지들을 ZIP으로 압축)
     * 
     * @param cssContent CSS 내용
     * @param request 커스터마이징 요청 데이터
     * @return ktheme 파일 경로
     */
    private String generateKthemeFile(String cssContent, CssCustomizationRequest request) {
        try {
            // CSS에서 사용된 이미지 파일명 추출
            Set<String> imageFiles = extractImageFilesFromCss(cssContent);
            
            if (imageFiles.isEmpty()) {
                log.info("이미지 파일이 없어서 CSS 파일만 반환합니다.");
                return null; // 이미지가 없으면 CSS 파일만 반환
            }

            // 파일명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String kthemeFileName = String.format("theme_%d_%s_%s.ktheme", 
                    request.getThemeId(), 
                    request.getUserEmail().replace("@", "_").replace(".", "_"), 
                    timestamp);
            
            // 프로젝트 루트의 generated-ktheme 디렉토리에 저장
            String projectRoot = System.getProperty("user.dir");
            String kthemeDir = projectRoot + "/generated-ktheme/";
            Files.createDirectories(Paths.get(kthemeDir));
            
            String kthemeFilePath = kthemeDir + kthemeFileName;
            
            // ZIP 파일 생성
            try (FileOutputStream fos = new FileOutputStream(kthemeFilePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                
                // 1. CSS 파일 추가
                ZipEntry cssEntry = new ZipEntry("KakaoTalkTheme.css");
                zos.putNextEntry(cssEntry);
                zos.write(cssContent.getBytes("UTF-8"));
                zos.closeEntry();
                
                // 2. Images 폴더 생성 및 이미지 파일들 추가
                for (String imageFile : imageFiles) {
                    try {
                        byte[] imageData;
                        try {
                            // S3에서 이미지 다운로드 시도
                            String s3Key = "theme-assets/" + imageFile;
                            imageData = s3FileManager.downloadFile(s3Key, THEME_BUCKET_NAME);
                            log.debug("S3에서 이미지 다운로드 성공: {}", imageFile);
                        } catch (Exception s3Exception) {
                            log.warn("S3 다운로드 실패, 더미 이미지 사용: {}", imageFile, s3Exception);
                            // S3 실패 시 더미 이미지 생성 (1x1 PNG)
                            imageData = createDummyImage();
                        }
                        
                        // ZIP에 이미지 추가
                        ZipEntry imageEntry = new ZipEntry("Images/" + imageFile);
                        zos.putNextEntry(imageEntry);
                        zos.write(imageData);
                        zos.closeEntry();
                        
                        log.debug("이미지 파일 추가: {}", imageFile);
                        
                    } catch (Exception e) {
                        log.warn("이미지 파일 처리 실패: {}", imageFile, e);
                        // 개별 이미지 실패는 전체 프로세스를 중단하지 않음
                    }
                }
            }
            
            log.info("ktheme 파일 생성 완료: {}", kthemeFilePath);
            return kthemeFilePath;
            
        } catch (Exception e) {
            log.error("ktheme 파일 생성 실패", e);
            return null;
        }
    }

    /**
     * CSS 내용에서 사용된 이미지 파일명들을 추출
     * 
     * @param cssContent CSS 내용
     * @return 이미지 파일명 목록
     */
    private Set<String> extractImageFilesFromCss(String cssContent) {
        Set<String> imageFiles = new HashSet<>();
        
        // CloudFront URL 패턴에서 파일명 추출: https://d9nlqn33m8hgw.cloudfront.net/theme-assets/filename.ext
        Pattern urlPattern = Pattern.compile("https://[^/]+\\.cloudfront\\.net/theme-assets/([^;\\s)]+)");
        Matcher matcher = urlPattern.matcher(cssContent);
        
        while (matcher.find()) {
            String fileName = matcher.group(1);
            if (isImageFile(fileName)) {
                imageFiles.add(fileName);
                log.debug("추출된 이미지 파일: {}", fileName);
            }
        }
        
        log.info("CSS에서 추출된 이미지 파일: {}", imageFiles);
        return imageFiles;
    }

    /**
     * 더미 이미지 생성 (1x1 투명 PNG)
     * S3 다운로드 실패 시 사용
     * 
     * @return 더미 이미지 바이트 배열
     */
    private byte[] createDummyImage() {
        // 1x1 투명 PNG 이미지 (Base64 디코딩)
        String base64Image = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        try {
            return java.util.Base64.getDecoder().decode(base64Image);
        } catch (Exception e) {
            log.error("더미 이미지 생성 실패", e);
            // 최소한의 PNG 헤더라도 반환
            return new byte[] {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31, 21, -60, -119, 0, 0, 0, 13, 73, 68, 65, 84, 120, -38, 99, -4, -1, -7, -104, 122, 0, 7, -126, 2, 127, 60, -56, 72, -17, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        }
    }
}