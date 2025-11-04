package com.komentum.theme.ios.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**

 * iOS 전용 CSS 옵션 관리 서비스
 * iOS KakaoTalk 테마의 CSS 스타일 옵션 초기화 및 관리를 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IosCssOptionService {

    private final ColorStyleRepository colorStyleRepository;

    /**
     * 애플리케이션 시작 시 iOS CSS 옵션 초기화
     */
    @PostConstruct
    public void init() {
        // iOS 옵션이 없는 경우에만 초기화
        if (colorStyleRepository.countByPlatform(Platform.IOS) == 0) {
            initializeIosCssOptions();
        }
    }

    /**
     * iOS CSS 옵션 초기화
     * iOS KakaoTalk 테마에 필요한 모든 CSS 스타일 옵션을 데이터베이스에 생성
     */
    @Transactional
    public void initializeIosCssOptions() {
        log.info("iOS CSS 옵션을 초기화합니다.");

        // 기존 iOS 옵션 삭제 (재초기화를 위해)
        colorStyleRepository.deleteByPlatform(Platform.IOS);

        // ManifestStyle 카테고리
        createCssOption("테마 이름", Platform.IOS, ":root", "-kakaotalk-theme-name", "text");
        createCssOption("테마 버전", Platform.IOS, ":root", "-kakaotalk-theme-version", "text");
        createCssOption("테마 ID", Platform.IOS, ":root", "-kakaotalk-theme-id", "text");
        createCssOption("작성자 이름", Platform.IOS, ":root", "-kakaotalk-author-name", "text");

        // TabBarStyle 카테고리
        createCssOption("탭바 배경 컬러", Platform.IOS, "TabBarStyle", "background-color", "color");
        createCssOption("탭바 테두리 컬러", Platform.IOS, "TabBarStyle", "border-top-color", "color");
        createCssOption("탭바 선택된 아이템 컬러", Platform.IOS, "TabBarStyle", "-ios-selected-item-tint-color", "color");
        createCssOption("탭바 기본 아이템 컬러", Platform.IOS, "TabBarStyle", "-ios-unselected-item-tint-color", "color");

        // Header 카테고리
        createCssOption("헤더 배경 컬러", Platform.IOS, "HeaderStyle", "background-color", "color");
        createCssOption("헤더 텍스트 컬러", Platform.IOS, "LabelStyle-HeaderTitle", "-ios-text-color", "color");
        createCssOption("헤더 버튼 컬러", Platform.IOS, "ButtonStyle-Header", "-ios-tint-color", "color");
        createCssOption("헤더 검색바 배경 컬러", Platform.IOS, "SearchBarStyle-Header", "background-color", "color");
        createCssOption("헤더 검색바 텍스트 컬러", Platform.IOS, "SearchBarStyle-Header", "-ios-text-color", "color");

        // MainViewStyle 카테고리
        createCssOption("메인 뷰 배경 컬러", Platform.IOS, "MainViewStyle-Primary", "background-color", "color");
        createCssOption("메인 뷰 배경 이미지", Platform.IOS, "MainViewStyle-Primary", "-ios-background-image", "image");
        createCssOption("메인 뷰 헤더 배경 컬러", Platform.IOS, "MainViewStyle-Primary", "header-background-color", "color");

        // SectionStyle 카테고리
        createCssOption("섹션 배경 컬러", Platform.IOS, "SectionStyle", "background-color", "color");
        createCssOption("섹션 헤더 텍스트 컬러", Platform.IOS, "LabelStyle-SectionHeader", "-ios-text-color", "color");
        createCssOption("섹션 푸터 텍스트 컬러", Platform.IOS, "LabelStyle-SectionFooter", "-ios-text-color", "color");

        // Feature 카테고리
        createCssOption("피처 셀 배경 컬러", Platform.IOS, "FeatureCellStyle", "background-color", "color");
        createCssOption("피처 셀 선택된 배경 컬러", Platform.IOS, "FeatureCellStyle", "-ios-selected-background-color", "color");
        createCssOption("피처 셀 텍스트 컬러", Platform.IOS, "LabelStyle-FeatureCell", "-ios-text-color", "color");
        createCssOption("피처 셀 서브 텍스트 컬러", Platform.IOS, "LabelStyle-FeatureCellSub", "-ios-text-color", "color");

        // Button 카테고리
        createCssOption("기본 버튼 배경 컬러", Platform.IOS, "ButtonStyle-Default", "background-color", "color");
        createCssOption("기본 버튼 텍스트 컬러", Platform.IOS, "ButtonStyle-Default", "-ios-title-color", "color");
        createCssOption("강조 버튼 배경 컬러", Platform.IOS, "ButtonStyle-Emphasized", "background-color", "color");
        createCssOption("강조 버튼 텍스트 컬러", Platform.IOS, "ButtonStyle-Emphasized", "-ios-title-color", "color");

        // DefaultProfile 카테고리
        createCssOption("기본 프로필 배경 컬러", Platform.IOS, "DefaultProfileStyle", "background-color", "color");
        createCssOption("기본 프로필 텍스트 컬러", Platform.IOS, "DefaultProfileStyle", "-ios-text-color", "color");

        // ChatRoomStyle 카테고리
        createCssOption("채팅방 배경 컬러", Platform.IOS, "ChatRoomStyle", "background-color", "color");
        createCssOption("채팅방 배경 이미지", Platform.IOS, "ChatRoomStyle", "-ios-background-image", "image");
        createCssOption("채팅방 입력바 배경 컬러", Platform.IOS, "InputBarStyle", "background-color", "color");
        createCssOption("채팅방 입력바 텍스트 컬러", Platform.IOS, "InputBarStyle", "-ios-text-color", "color");
        createCssOption("채팅방 입력바 플레이스홀더 컬러", Platform.IOS, "InputBarStyle", "-ios-placeholder-text-color", "color");

        // MessageCellStyle 카테고리
        createCssOption("보낸 메시지 말풍선 배경 컬러", Platform.IOS, "MessageCellStyle-Send", "background-color", "color");
        createCssOption("보낸 메시지 말풍선 이미지", Platform.IOS, "MessageCellStyle-Send", "-ios-background-image", "image");
        createCssOption("보낸 메시지 텍스트 컬러", Platform.IOS, "MessageCellStyle-Send", "-ios-text-color", "color");
        createCssOption("보낸 메시지 읽지않음 숫자 컬러", Platform.IOS, "MessageCellStyle-Send", "-ios-unread-text-color", "color");
        createCssOption("받은 메시지 말풍선 배경 컬러", Platform.IOS, "MessageCellStyle-Receive", "background-color", "color");
        createCssOption("받은 메시지 말풍선 이미지", Platform.IOS, "MessageCellStyle-Receive", "-ios-background-image", "image");
        createCssOption("받은 메시지 텍스트 컬러", Platform.IOS, "MessageCellStyle-Receive", "-ios-text-color", "color");
        createCssOption("받은 메시지 읽지않음 숫자 컬러", Platform.IOS, "MessageCellStyle-Receive", "-ios-unread-text-color", "color");

        // Passcode 카테고리
        createCssOption("잠금화면 배경 컬러", Platform.IOS, "BackgroundStyle-Passcode", "background-color", "color");
        createCssOption("잠금화면 배경 이미지", Platform.IOS, "BackgroundStyle-Passcode", "-ios-background-image", "image");
        createCssOption("잠금화면 텍스트 컬러", Platform.IOS, "LabelStyle-PasscodeTitle", "-ios-text-color", "color");
        createCssOption("키패드 배경 컬러", Platform.IOS, "PasscodeStyle", "-ios-keypad-background-color", "color");
        createCssOption("키패드 숫자 텍스트 컬러", Platform.IOS, "PasscodeStyle", "-ios-keypad-text-normal-color", "color");

        // Notification 카테고리
        createCssOption("메시지 알림 배너 배경 컬러", Platform.IOS, "BackgroundStyle-MessageNotificationBar", "background-color", "color");
        createCssOption("메시지 알림 배너 이름 컬러", Platform.IOS, "LabelStyle-MessageNotificationBarName", "-ios-text-color", "color");
        createCssOption("메시지 알림 배너 텍스트 컬러", Platform.IOS, "LabelStyle-MessageNotificationBarMessage", "-ios-text-color", "color");

        // DirectShare 카테고리
        createCssOption("전달 완료 배너 배경 컬러", Platform.IOS, "BackgroundStyle-DirectShareBar", "background-color", "color");
        createCssOption("전달 완료 배너 이름 컬러", Platform.IOS, "LabelStyle-DirectShareBarName", "-ios-text-color", "color");
        createCssOption("전달 완료 배너 텍스트 컬러", Platform.IOS, "LabelStyle-DirectShareBarMessage", "-ios-text-color", "color");

        // BottomBanner 카테고리
        createCssOption("탭 배너 배경 컬러", Platform.IOS, "BottomBannerStyle", "background-color", "color");

        long count = colorStyleRepository.countByPlatform(Platform.IOS);
        log.info("iOS CSS 옵션 초기화 완료: {} 개 옵션", count);
    }

    /**
     * iOS CSS 옵션 생성
     */
    private void createCssOption(String explain, Platform platform, String styleSheetPath, String styleElementName, String stylePropsName) {
        ColorStyle colorStyle = ColorStyle.builder()
                .explain(explain)
                .platform(platform)
                .styleSheetPath(styleSheetPath)
                .styleElementName(styleElementName)
                .stylePropsName(stylePropsName)
                .build();
        colorStyleRepository.save(colorStyle);
    }

    /**
     * iOS CSS 옵션을 카테고리별로 조회
     */
    @Transactional(readOnly = true)
    public Map<String, List<ColorStyle>> getIosCssOptionsByCategory() {
        return colorStyleRepository.findByPlatform(Platform.IOS).stream()
                .collect(Collectors.groupingBy(this::extractIosCategory));
    }

    /**
     * 특정 카테고리의 iOS CSS 옵션 조회
     */
    @Transactional(readOnly = true)
    public List<ColorStyle> getIosCssOptionsByCategory(String category) {
        return colorStyleRepository.findByPlatform(Platform.IOS).stream()
                .filter(option -> extractIosCategory(option).equals(category))
                .collect(Collectors.toList());
    }

    /**
     * 모든 iOS CSS 옵션 조회
     */
    @Transactional(readOnly = true)
    public List<ColorStyle> getAllIosCssOptions() {
        return colorStyleRepository.findByPlatform(Platform.IOS);
    }

    /**
     * iOS CSS 옵션의 카테고리 추출
     */
    private String extractIosCategory(ColorStyle colorStyle) {
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
