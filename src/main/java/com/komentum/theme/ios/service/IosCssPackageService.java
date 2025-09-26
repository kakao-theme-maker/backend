package com.komentum.theme.ios.service;

import com.komentum.theme.theme.dto.ThemeComponentDto;
import com.komentum.theme.theme.dto.ThemeImageDto;
import com.komentum.theme.theme.service.ThemeCssGeneratorService;
import com.komentum.theme.theme.service.ThemeRetrieveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IosCssPackageService {

    private final ThemeCssGeneratorService themeCssGeneratorService;
    private final ThemeRetrieveService themeRetrieveService;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * iOS .ktheme 패키지 생성
     */
    public String generateKthemePackage(Integer themeId) {
        try {
            log.info("iOS .ktheme 패키지 생성 시작: themeId={}", themeId);

            // 테마 데이터 조회
            ThemeComponentDto theme = themeRetrieveService.getThemeById(themeId);
            
            // CSS 생성
            String css = themeCssGeneratorService.generateThemeCss(themeId);
            
            // 임시 디렉토리 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String packageName = String.format("theme_%d_%s_%s", 
                themeId, 
                theme.getThemeName().replaceAll("[^a-zA-Z0-9가-힣]", "_"),
                timestamp
            );
            
            Path tempDir = createTempDirectory(packageName);
            Path imagesDir = tempDir.resolve("Images");
            Files.createDirectories(imagesDir);
            
            // CSS 파일 저장
            Path cssFile = tempDir.resolve("KakaoTalkTheme.css");
            Files.write(cssFile, css.getBytes(StandardCharsets.UTF_8));
            
            // 이미지 파일들 다운로드 및 저장
            downloadThemeImages(theme, imagesDir);
            
            // ZIP 패키지 생성
            String packagePath = createZipPackage(tempDir, packageName);
            
            // 임시 디렉토리 정리
            cleanupTempDirectory(tempDir);
            
            log.info("iOS .ktheme 패키지 생성 완료: {}", packagePath);
            return packagePath;
            
        } catch (Exception e) {
            log.error("iOS .ktheme 패키지 생성 실패: themeId={}", themeId, e);
            throw new RuntimeException(".ktheme 패키지 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 임시 디렉토리 생성
     */
    private Path createTempDirectory(String packageName) throws IOException {
        Path generatedDir = Paths.get("generated-ktheme");
        Files.createDirectories(generatedDir);
        
        Path tempDir = generatedDir.resolve(packageName);
        Files.createDirectories(tempDir);
        
        return tempDir;
    }

    /**
     * 테마 이미지들 다운로드 및 저장 (현재 구조에서는 기본 이미지 생성)
     */
    private void downloadThemeImages(ThemeComponentDto theme, Path imagesDir) {
        List<ThemeImageDto> images = theme.getImages();
        if (images == null || images.isEmpty()) {
            log.warn("테마에 이미지가 없어 기본 이미지를 생성합니다: themeId={}", theme.getThemeComponentId());
            try {
                createDefaultImages(imagesDir);
            } catch (IOException e) {
                log.error("기본 이미지 생성 실패", e);
            }
            return;
        }

        // 현재 ThemeImageDto에는 designComponentId만 있으므로 기본 이미지 생성
        try {
            createDefaultImages(imagesDir);
            log.info("기본 이미지 생성 완료: {} 개 이미지", images.size());
        } catch (IOException e) {
            log.error("기본 이미지 생성 실패", e);
        }
    }

    /**
     * 개별 이미지 다운로드 및 저장
     */
    private void downloadAndSaveImage(String imageUrl, String imageName, Path imagesDir) throws Exception {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            log.warn("이미지 URL이 비어있습니다: {}", imageName);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(imageUrl))
            .GET()
            .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            Path imagePath = imagesDir.resolve(imageName);
            Files.write(imagePath, response.body());
            log.debug("이미지 다운로드 완료: {}", imageName);
        } else {
            log.warn("이미지 다운로드 실패: {} (HTTP {})", imageName, response.statusCode());
        }
    }

    /**
     * ZIP 패키지 생성
     */
    private String createZipPackage(Path sourceDir, String packageName) throws IOException {
        String outputDir = "generated-ktheme";
        Files.createDirectories(Paths.get(outputDir));
        
        String zipFileName = packageName + ".ktheme";
        Path zipPath = Paths.get(outputDir, zipFileName);
        
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(sourceDir)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        String entryName = sourceDir.relativize(path).toString();
                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(path, zipOut);
                        zipOut.closeEntry();
                    } catch (IOException e) {
                        log.error("ZIP 엔트리 추가 실패: {}", path, e);
                    }
                });
        }
        
        return zipPath.toString();
    }

    /**
     * 임시 디렉토리 정리
     */
    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                .sorted((path1, path2) -> path2.getNameCount() - path1.getNameCount()) // 깊은 것부터 삭제
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        log.warn("임시 파일 삭제 실패: {}", path, e);
                    }
                });
        } catch (IOException e) {
            log.warn("임시 디렉토리 정리 실패: {}", tempDir, e);
        }
    }

    /**
     * 기본 아이콘 이미지 생성 (이미지가 없을 때 사용)
     */
    private void createDefaultImages(Path imagesDir) throws IOException {
        // 기본 이미지들의 파일명 리스트
        String[] defaultImages = {
            "default-bg.png",
            "default-icon.png", 
            "home-icon.png",
            "home-selected.png",
            "friends-icon.png",
            "chat-icon.png",
            "send-bubble.png",
            "receive-bubble.png"
        };

        for (String imageName : defaultImages) {
            Path imagePath = imagesDir.resolve(imageName);
            if (!Files.exists(imagePath)) {
                // 간단한 1x1 투명 PNG 생성 (실제로는 기본 이미지 리소스를 복사해야 함)
                createPlaceholderImage(imagePath);
            }
        }
    }

    /**
     * 플레이스홀더 이미지 생성 (실제 구현에서는 기본 이미지 리소스 사용)
     */
    private void createPlaceholderImage(Path imagePath) throws IOException {
        // 최소한의 PNG 헤더 (1x1 투명 픽셀)
        byte[] pngData = {
            (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4,
            (byte)0x89, 0x00, 0x00, 0x00, 0x0B, 0x49, 0x44, 0x41,
            0x54, 0x08, 0x1D, 0x01, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42, 0x60, (byte)0x82
        };
        
        Files.write(imagePath, pngData);
    }
}