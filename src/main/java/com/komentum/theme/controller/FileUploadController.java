package com.komentum.theme.controller;

import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.service.DesignComponentService;
import com.komentum.theme.utils.S3FileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3FileManager s3FileManager;
    private final DesignComponentService designComponentService;

    @Value("${aws.s3.theme-bucket-name}")
    private String themeBucketName;

    /**
     * 테마 이미지 파일을 S3에 업로드하고 디자인컴포넌트로 자동 등록
     * 
     * @param file 업로드할 이미지 파일
     * @param fileName 파일명 (선택사항, 없으면 원본 파일명 사용)
     * @param userEmail 사용자 이메일 (필수)
     * @param isPublic 공개 여부 (선택사항, 기본값: false)
     * @param componentTypeId 컴포넌트 타입 ID (선택사항, 기본값: 1)
     * @return 업로드된 파일 정보 및 디자인컴포넌트 정보
     */
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, Object>> uploadThemeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam("userEmail") String userEmail,
            @RequestParam(value = "isPublic", required = false, defaultValue = "false") Boolean isPublic,
            @RequestParam(value = "componentTypeId", required = false, defaultValue = "1") Integer componentTypeId) {
        
        try {
            // 파일 검증
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("파일이 비어있습니다."));
            }
            
            // 이미지 파일 확장자 검증
            String originalFileName = file.getOriginalFilename();
            if (!isImageFile(originalFileName)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("이미지 파일만 업로드 가능합니다. (png, jpg, jpeg, svg, gif, webp)"));
            }
            
            // 파일명 결정
            String uploadFileName = fileName != null ? fileName : originalFileName;
            String s3Key = "theme-assets/" + uploadFileName;
            
            // S3에 업로드
            byte[] fileBytes = file.getBytes();
            String cloudFrontUrl = s3FileManager.uploadFile(fileBytes, s3Key, themeBucketName);
            
            log.info("이미지 파일 업로드 성공: {} -> {}", originalFileName, cloudFrontUrl);
            
            // 디자인컴포넌트 자동 생성
            CreateDesignComponentRequest componentRequest = new CreateDesignComponentRequest();
            componentRequest.setUserEmail(userEmail);
            componentRequest.setComponentTypeId(componentTypeId);
            componentRequest.setImageUrl(cloudFrontUrl);
            componentRequest.setIsPublic(isPublic);
            
            DesignComponentDto designComponent = designComponentService.createDesignComponent(componentRequest);
            
            log.info("디자인컴포넌트 자동 생성 완료: ID={}, User={}", 
                    designComponent.getDesignComponentId(), userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이미지 업로드 및 디자인컴포넌트 생성이 완료되었습니다.");
            response.put("originalFileName", originalFileName);
            response.put("uploadedFileName", uploadFileName);
            response.put("s3Key", s3Key);
            response.put("url", cloudFrontUrl);
            response.put("fileSize", file.getSize());
            
            // 생성된 디자인컴포넌트 정보 추가
            response.put("designComponent", Map.of(
                "designComponentId", designComponent.getDesignComponentId(),
                "componentTypeId", designComponent.getComponentType().getComponentTypeId(),
                "imageUrl", designComponent.getImageUrl(),
                "isPublic", designComponent.getIsPublic(),
                "createdAt", designComponent.getCreatedAt()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("이미지 업로드에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 여러 이미지 파일을 한 번에 업로드
     */
    @PostMapping("/upload-images")
    public ResponseEntity<Map<String, Object>> uploadThemeImages(
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            if (files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("파일이 없습니다."));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", files.length + "개의 이미지 업로드가 완료되었습니다.");
            response.put("uploadedFiles", new HashMap<>());
            
            Map<String, Object> uploadedFiles = (Map<String, Object>) response.get("uploadedFiles");
            
            for (MultipartFile file : files) {
                if (!file.isEmpty() && isImageFile(file.getOriginalFilename())) {
                    String s3Key = "theme-assets/" + file.getOriginalFilename();
                    String cloudFrontUrl = s3FileManager.uploadFile(file.getBytes(), s3Key, themeBucketName);
                    
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("url", cloudFrontUrl);
                    fileInfo.put("size", file.getSize());
                    uploadedFiles.put(file.getOriginalFilename(), fileInfo);
                    
                    log.info("이미지 파일 업로드 성공: {} -> {}", file.getOriginalFilename(), cloudFrontUrl);
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("다중 이미지 업로드 실패", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("이미지 업로드에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * S3에서 파일 삭제
     */
    @DeleteMapping("/delete-image")
    public ResponseEntity<Map<String, Object>> deleteThemeImage(
            @RequestParam("fileName") String fileName) {
        
        try {
            String s3Key = "theme-assets/" + fileName;
            s3FileManager.deleteFile(s3Key, themeBucketName);
            
            log.info("이미지 파일 삭제 성공: {}", s3Key);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "이미지 삭제가 완료되었습니다.");
            response.put("deletedFile", fileName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("이미지 삭제 실패", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("이미지 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 이미지 파일 확장자 검증
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
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}