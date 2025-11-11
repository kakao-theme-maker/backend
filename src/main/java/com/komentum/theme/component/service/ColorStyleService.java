package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.CreateColorStyleRequest;
import com.komentum.theme.component.dto.UpdateColorStyleRequest;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 플랫폼 중립적 ColorStyle 관리 서비스
 * 모든 플랫폼의 ColorStyle에 대한 기본적인 CRUD 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColorStyleService {

    private final ColorStyleRepository colorStyleRepository;

    /**
     * ColorStyle 생성 (DTO 버전)
     */
    @Transactional
    public ColorStyle createColorStyle(CreateColorStyleRequest request) {
        ColorStyle colorStyle = ColorStyle.builder()
                .explain(request.getExplain())
                .platform(request.getPlatform())
                .styleSheetPath(request.getStyleSheetPath())
                .styleElementName(request.getStyleElementName())
                .stylePropsName(request.getStylePropsName())
                .build();
        return colorStyleRepository.save(colorStyle);
    }

    /**
     * ColorStyle 생성 (도메인 객체 버전)
     */
    @Transactional
    public ColorStyle createColorStyle(ColorStyle colorStyle) {
        return colorStyleRepository.save(colorStyle);
    }

    /**
     * 모든 ColorStyle 조회
     */
    @Transactional(readOnly = true)
    public List<ColorStyle> getAllColorStyles() {
        return colorStyleRepository.findAll();
    }

    /**
     * 플랫폼별 ColorStyle 조회
     */
    @Transactional(readOnly = true)
    public List<ColorStyle> getColorStylesByPlatform(Platform platform) {
        return colorStyleRepository.findByPlatform(platform);
    }

    /**
     * 플랫폼별 ColorStyle 개수 조회
     */
    @Transactional(readOnly = true)
    public long countColorStylesByPlatform(Platform platform) {
        return colorStyleRepository.countByPlatform(platform);
    }

    /**
     * ColorStyle ID로 조회
     */
    @Transactional(readOnly = true)
    public ColorStyle getColorStyleById(Integer colorStyleId) {
        return colorStyleRepository.findById(colorStyleId)
                .orElseThrow(() -> new RuntimeException("ColorStyle을 찾을 수 없습니다: " + colorStyleId));
    }

    /**
     * ColorStyle 수정 (DTO 버전)
     */
    @Transactional
    public ColorStyle updateColorStyle(Integer colorStyleId, UpdateColorStyleRequest request) {
        ColorStyle existingColorStyle = getColorStyleById(colorStyleId);
        
        // 필요한 필드들 업데이트
        if (request.getExplain() != null) {
            existingColorStyle.setExplain(request.getExplain());
        }
        if (request.getStyleSheetPath() != null) {
            existingColorStyle.setStyleSheetPath(request.getStyleSheetPath());
        }
        if (request.getStyleElementName() != null) {
            existingColorStyle.setStyleElementName(request.getStyleElementName());
        }
        if (request.getStylePropsName() != null) {
            existingColorStyle.setStylePropsName(request.getStylePropsName());
        }
        
        return colorStyleRepository.save(existingColorStyle);
    }

    /**
     * ColorStyle 수정 (도메인 객체 버전)
     */
    @Transactional
    public ColorStyle updateColorStyle(Integer colorStyleId, ColorStyle updateData) {
        ColorStyle existingColorStyle = getColorStyleById(colorStyleId);
        
        // 필요한 필드들 업데이트
        if (updateData.getExplain() != null) {
            existingColorStyle.setExplain(updateData.getExplain());
        }
        if (updateData.getStyleSheetPath() != null) {
            existingColorStyle.setStyleSheetPath(updateData.getStyleSheetPath());
        }
        if (updateData.getStyleElementName() != null) {
            existingColorStyle.setStyleElementName(updateData.getStyleElementName());
        }
        if (updateData.getStylePropsName() != null) {
            existingColorStyle.setStylePropsName(updateData.getStylePropsName());
        }
        
        return colorStyleRepository.save(existingColorStyle);
    }

    /**
     * ColorStyle 삭제
     */
    @Transactional
    public void deleteColorStyle(Integer colorStyleId) {
        colorStyleRepository.deleteById(colorStyleId);
    }

    /**
     * 플랫폼별 ColorStyle 모두 삭제
     */
    @Transactional
    public void deleteColorStylesByPlatform(Platform platform) {
        colorStyleRepository.deleteByPlatform(platform);
    }

    /**
     * 전체 ColorStyle을 카테고리별로 그룹화
     */
    @Transactional(readOnly = true)
    public Map<String, List<ColorStyle>> getColorStylesByCategory() {
        return colorStyleRepository.findAll().stream()
                .collect(Collectors.groupingBy(this::extractCategory));
    }

    /**
     * 특정 카테고리의 ColorStyle 조회
     */
    @Transactional(readOnly = true)
    public List<ColorStyle> getColorStylesByCategory(String category) {
        return colorStyleRepository.findAll().stream()
                .filter(option -> extractCategory(option).equals(category))
                .collect(Collectors.toList());
    }

    /**
     * ColorStyle에서 카테고리 추출 (범용)
     */
    private String extractCategory(ColorStyle colorStyle) {
        String styleSheetPath = colorStyle.getStyleSheetPath();
        if (styleSheetPath.contains(":root")) return "Manifest";
        if (styleSheetPath.contains("TabBar")) return "TabBar";
        if (styleSheetPath.contains("Header")) return "Header";
        if (styleSheetPath.contains("MainView")) return "MainView";
        if (styleSheetPath.contains("Section")) return "Section";
        if (styleSheetPath.contains("Feature")) return "Feature";
        if (styleSheetPath.contains("Button")) return "Button";
        if (styleSheetPath.contains("DefaultProfile")) return "DefaultProfile";
        if (styleSheetPath.contains("ChatRoom") || styleSheetPath.contains("InputBar")) return "ChatRoom";
        if (styleSheetPath.contains("MessageCell")) return "Message";
        if (styleSheetPath.contains("Passcode")) return "Passcode";
        if (styleSheetPath.contains("MessageNotification")) return "Notification";
        if (styleSheetPath.contains("DirectShare")) return "DirectShare";
        if (styleSheetPath.contains("BottomBanner")) return "BottomBanner";
        return "기타";
    }
}