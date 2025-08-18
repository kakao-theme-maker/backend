package com.komentum.theme.component.service;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.repository.ColorStyleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColorStyleService {

    private final ColorStyleRepository colorStyleRepository;

    @Bean
    public ApplicationRunner initializeColorStyles() {
        return args -> {
            if (colorStyleRepository.count() == 0) {
                log.info("ColorStyle 데이터를 초기화합니다.");
                initializeCssOptions();
            }
        };
    }

    @Transactional
    public void initializeCssOptions() {
        // exampleCss.css 기반으로 CSS 옵션들을 ColorStyle 테이블에 저장
        
        // Manifest 카테고리
        createCssOption("테마 이름", "ManifestStyle|-kakaotalk-theme-name", "ManifestStyle|-kakaotalk-theme-name");
        createCssOption("테마 버전", "ManifestStyle|-kakaotalk-theme-version", "ManifestStyle|-kakaotalk-theme-version");
        createCssOption("테마 URL", "ManifestStyle|-kakaotalk-theme-url", "ManifestStyle|-kakaotalk-theme-url");
        createCssOption("테마 제작자", "ManifestStyle|-kakaotalk-author-name", "ManifestStyle|-kakaotalk-author-name");
        createCssOption("테마 ID", "ManifestStyle|-kakaotalk-theme-id", "ManifestStyle|-kakaotalk-theme-id");

        // TabBar 카테고리
        createCssOption("메인탭 배경 컬러", "TabBarStyle-Main|background-color", "TabBarStyle-Main|background-color");
        createCssOption("메인탭 배경 이미지", "TabBarStyle-Main|-ios-background-image", "TabBarStyle-Main|-android-background-image");
        createCssOption("홈탭 아이콘", "TabBarStyle-Main|-ios-home-normal-icon-image", "TabBarStyle-Main|-android-home-normal-icon-image");
        createCssOption("홈탭 선택 아이콘", "TabBarStyle-Main|-ios-home-selected-icon-image", "TabBarStyle-Main|-android-home-selected-icon-image");
        createCssOption("친구탭 아이콘", "TabBarStyle-Main|-ios-friends-normal-icon-image", "TabBarStyle-Main|-android-friends-normal-icon-image");
        createCssOption("친구탭 선택 아이콘", "TabBarStyle-Main|-ios-friends-selected-icon-image", "TabBarStyle-Main|-android-friends-selected-icon-image");
        createCssOption("채팅탭 아이콘", "TabBarStyle-Main|-ios-chats-normal-icon-image", "TabBarStyle-Main|-android-chats-normal-icon-image");
        createCssOption("채팅탭 선택 아이콘", "TabBarStyle-Main|-ios-chats-selected-icon-image", "TabBarStyle-Main|-android-chats-selected-icon-image");

        // Header 카테고리
        createCssOption("헤더 텍스트 컬러", "HeaderStyle-Main|-ios-text-color", "HeaderStyle-Main|-android-text-color");
        createCssOption("탭 텍스트 컬러", "HeaderStyle-Main|-ios-tab-text-color", "HeaderStyle-Main|-android-tab-text-color");
        createCssOption("탭 선택 텍스트 컬러", "HeaderStyle-Main|-ios-tab-highlighted-text-color", "HeaderStyle-Main|-android-tab-highlighted-text-color");

        // MainView 카테고리
        createCssOption("메인뷰 배경 컬러", "MainViewStyle-Primary|background-color", "MainViewStyle-Primary|background-color");
        createCssOption("메인뷰 배경 이미지", "MainViewStyle-Primary|-ios-background-image", "MainViewStyle-Primary|-android-background-image");
        createCssOption("이름/타이틀 컬러", "MainViewStyle-Primary|-ios-text-color", "MainViewStyle-Primary|-android-text-color");
        createCssOption("이름/타이틀 프레스 컬러", "MainViewStyle-Primary|-ios-highlighted-text-color", "MainViewStyle-Primary|-android-highlighted-text-color");
        createCssOption("상태메시지 컬러", "MainViewStyle-Primary|-ios-description-text-color", "MainViewStyle-Primary|-android-description-text-color");
        createCssOption("라스트메시지 컬러", "MainViewStyle-Primary|-ios-paragraph-text-color", "MainViewStyle-Primary|-android-paragraph-text-color");
        createCssOption("리스트 배경 컬러", "MainViewStyle-Primary|-ios-normal-background-color", "MainViewStyle-Primary|-android-normal-background-color");
        createCssOption("리스트 프레스 배경 컬러", "MainViewStyle-Primary|-ios-selected-background-color", "MainViewStyle-Primary|-android-selected-background-color");
        createCssOption("3탭/4탭 배경 컬러", "MainViewStyle-Secondary|background-color", "MainViewStyle-Secondary|background-color");

        // Section 카테고리
        createCssOption("섹션 보더 컬러", "SectionTitleStyle-Main|border-color", "SectionTitleStyle-Main|border-color");
        createCssOption("섹션 타이틀 컬러", "SectionTitleStyle-Main|-ios-text-color", "SectionTitleStyle-Main|-android-text-color");

        // Feature 카테고리
        createCssOption("서비스 버튼 컬러", "FeatureStyle-Primary|-ios-text-color", "FeatureStyle-Primary|-android-text-color");

        // Button 카테고리
        createCssOption("친구추가 버튼 이미지", "ButtonStyle-AddFriend|-ios-image", "ButtonStyle-AddFriend|-android-image");

        // DefaultProfile 카테고리
        createCssOption("기본 프로필 이미지", "DefaultProfileStyle|-ios-profile-images", "DefaultProfileStyle|-android-profile-images");

        // ChatRoom 카테고리
        createCssOption("채팅방 배경 컬러", "BackgroundStyle-ChatRoom|background-color", "BackgroundStyle-ChatRoom|background-color");
        createCssOption("채팅방 배경 이미지", "BackgroundStyle-ChatRoom|-ios-background-image", "BackgroundStyle-ChatRoom|-android-background-image");
        createCssOption("인풋바 배경 컬러", "InputBarStyle-Chat|background-color", "InputBarStyle-Chat|background-color");
        createCssOption("보내기 버튼 배경 컬러", "InputBarStyle-Chat|-ios-send-normal-background-color", "InputBarStyle-Chat|-android-send-normal-background-color");
        createCssOption("보내기 버튼 아이콘 컬러", "InputBarStyle-Chat|-ios-send-normal-foreground-color", "InputBarStyle-Chat|-android-send-normal-foreground-color");
        createCssOption("메뉴 버튼 아이콘 컬러", "InputBarStyle-Chat|-ios-button-normal-foreground-color", "InputBarStyle-Chat|-android-button-normal-foreground-color");

        // Message 카테고리
        createCssOption("보낸 메시지 말풍선 이미지", "MessageCellStyle-Send|-ios-background-image", "MessageCellStyle-Send|-android-background-image");
        createCssOption("보낸 메시지 텍스트 컬러", "MessageCellStyle-Send|-ios-text-color", "MessageCellStyle-Send|-android-text-color");
        createCssOption("보낸 메시지 읽지않음 숫자 컬러", "MessageCellStyle-Send|-ios-unread-text-color", "MessageCellStyle-Send|-android-unread-text-color");
        createCssOption("받은 메시지 말풍선 이미지", "MessageCellStyle-Receive|-ios-background-image", "MessageCellStyle-Receive|-android-background-image");
        createCssOption("받은 메시지 텍스트 컬러", "MessageCellStyle-Receive|-ios-text-color", "MessageCellStyle-Receive|-android-text-color");
        createCssOption("받은 메시지 읽지않음 숫자 컬러", "MessageCellStyle-Receive|-ios-unread-text-color", "MessageCellStyle-Receive|-android-unread-text-color");

        // Passcode 카테고리
        createCssOption("잠금화면 배경 컬러", "BackgroundStyle-Passcode|background-color", "BackgroundStyle-Passcode|background-color");
        createCssOption("잠금화면 배경 이미지", "BackgroundStyle-Passcode|-ios-background-image", "BackgroundStyle-Passcode|-android-background-image");
        createCssOption("잠금화면 텍스트 컬러", "LabelStyle-PasscodeTitle|-ios-text-color", "LabelStyle-PasscodeTitle|-android-text-color");
        createCssOption("키패드 배경 컬러", "PasscodeStyle|-ios-keypad-background-color", "PasscodeStyle|-android-keypad-background-color");
        createCssOption("키패드 숫자 텍스트 컬러", "PasscodeStyle|-ios-keypad-text-normal-color", "PasscodeStyle|-android-keypad-text-normal-color");

        // Notification 카테고리
        createCssOption("메시지 알림 배너 배경 컬러", "BackgroundStyle-MessageNotificationBar|background-color", "BackgroundStyle-MessageNotificationBar|background-color");
        createCssOption("메시지 알림 배너 이름 컬러", "LabelStyle-MessageNotificationBarName|-ios-text-color", "LabelStyle-MessageNotificationBarName|-android-text-color");
        createCssOption("메시지 알림 배너 텍스트 컬러", "LabelStyle-MessageNotificationBarMessage|-ios-text-color", "LabelStyle-MessageNotificationBarMessage|-android-text-color");

        // DirectShare 카테고리
        createCssOption("전달 완료 배너 배경 컬러", "BackgroundStyle-DirectShareBar|background-color", "BackgroundStyle-DirectShareBar|background-color");
        createCssOption("전달 완료 배너 이름 컬러", "LabelStyle-DirectShareBarName|-ios-text-color", "LabelStyle-DirectShareBarName|-android-text-color");
        createCssOption("전달 완료 배너 텍스트 컬러", "LabelStyle-DirectShareBarMessage|-ios-text-color", "LabelStyle-DirectShareBarMessage|-android-text-color");

        // BottomBanner 카테고리
        createCssOption("탭 배너 배경 컬러", "BottomBannerStyle|background-color", "BottomBannerStyle|background-color");

        log.info("ColorStyle 데이터 초기화 완료: {} 개 옵션", colorStyleRepository.count());
    }

    private void createCssOption(String explain, String iosStyleName, String androidStyleName) {
        ColorStyle colorStyle = ColorStyle.builder()
                .explain(explain)
                .iosStyleName(iosStyleName)
                .androidStyleName(androidStyleName)
                .build();
        colorStyleRepository.save(colorStyle);
    }

    public List<ColorStyle> getAllCssOptions() {
        return colorStyleRepository.findAll();
    }

    public Map<String, List<ColorStyle>> getCssOptionsByCategory() {
        return colorStyleRepository.findAll().stream()
                .collect(Collectors.groupingBy(this::extractCategory));
    }

    public List<ColorStyle> getCssOptionsByCategory(String category) {
        return colorStyleRepository.findAll().stream()
                .filter(option -> extractCategory(option).equals(category))
                .collect(Collectors.toList());
    }

    private String extractCategory(ColorStyle colorStyle) {
        String iosStyleName = colorStyle.getIosStyleName();
        if (iosStyleName.contains("Manifest")) return "Manifest";
        if (iosStyleName.contains("TabBar")) return "TabBar";
        if (iosStyleName.contains("Header")) return "Header";
        if (iosStyleName.contains("MainView")) return "MainView";
        if (iosStyleName.contains("Section")) return "Section";
        if (iosStyleName.contains("Feature")) return "Feature";
        if (iosStyleName.contains("Button")) return "Button";
        if (iosStyleName.contains("DefaultProfile")) return "DefaultProfile";
        if (iosStyleName.contains("ChatRoom") || iosStyleName.contains("InputBar")) return "ChatRoom";
        if (iosStyleName.contains("MessageCell")) return "Message";
        if (iosStyleName.contains("Passcode")) return "Passcode";
        if (iosStyleName.contains("MessageNotification")) return "Notification";
        if (iosStyleName.contains("DirectShare")) return "DirectShare";
        if (iosStyleName.contains("BottomBanner")) return "BottomBanner";
        return "기타";
    }

    public ColorStyle getCssOptionById(Integer colorTypeId) {
        return colorStyleRepository.findById(colorTypeId)
                .orElseThrow(() -> new RuntimeException("CSS 옵션을 찾을 수 없습니다: " + colorTypeId));
    }
}
