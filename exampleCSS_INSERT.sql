-- exampleCss.css 파일의 모든 선택자와 속성을 ColorStyle 테이블에 INSERT하는 SQL문

-- 기존 데이터 삭제 (선택사항)
-- DELETE FROM color_style;

-- ManifestStyle 선택자 (5개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('테마 이름', 'ManifestStyle|-kakaotalk-theme-name', 'ManifestStyle|-kakaotalk-theme-name'),
('테마 버전', 'ManifestStyle|-kakaotalk-theme-version', 'ManifestStyle|-kakaotalk-theme-version'),
('테마 URL', 'ManifestStyle|-kakaotalk-theme-url', 'ManifestStyle|-kakaotalk-theme-url'),
('테마 제작자', 'ManifestStyle|-kakaotalk-author-name', 'ManifestStyle|-kakaotalk-author-name'),
('테마 ID', 'ManifestStyle|-kakaotalk-theme-id', 'ManifestStyle|-kakaotalk-theme-id');

-- TabBarStyle-Main 선택자 (17개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('메인탭 배경 컬러', 'TabBarStyle-Main|background-color', 'TabBarStyle-Main|background-color'),
('메인탭 배경 이미지', 'TabBarStyle-Main|-ios-background-image', 'TabBarStyle-Main|-android-background-image'),
('홈탭 아이콘', 'TabBarStyle-Main|-ios-home-normal-icon-image', 'TabBarStyle-Main|-android-home-normal-icon-image'),
('홈탭 선택 아이콘', 'TabBarStyle-Main|-ios-home-selected-icon-image', 'TabBarStyle-Main|-android-home-selected-icon-image'),
('친구탭 아이콘', 'TabBarStyle-Main|-ios-friends-normal-icon-image', 'TabBarStyle-Main|-android-friends-normal-icon-image'),
('친구탭 선택 아이콘', 'TabBarStyle-Main|-ios-friends-selected-icon-image', 'TabBarStyle-Main|-android-friends-selected-icon-image'),
('채팅탭 아이콘', 'TabBarStyle-Main|-ios-chats-normal-icon-image', 'TabBarStyle-Main|-android-chats-normal-icon-image'),
('채팅탭 선택 아이콘', 'TabBarStyle-Main|-ios-chats-selected-icon-image', 'TabBarStyle-Main|-android-chats-selected-icon-image'),
('친구찾기탭 아이콘', 'TabBarStyle-Main|-ios-find-normal-icon-image', 'TabBarStyle-Main|-android-find-normal-icon-image'),
('친구찾기탭 선택 아이콘', 'TabBarStyle-Main|-ios-find-selected-icon-image', 'TabBarStyle-Main|-android-find-selected-icon-image'),
('채널탭 아이콘', 'TabBarStyle-Main|-ios-browse-normal-icon-image', 'TabBarStyle-Main|-android-browse-normal-icon-image'),
('채널탭 선택 아이콘', 'TabBarStyle-Main|-ios-browse-selected-icon-image', 'TabBarStyle-Main|-android-browse-selected-icon-image'),
('뷰탭 아이콘', 'TabBarStyle-Main|-ios-view-normal-icon-image', 'TabBarStyle-Main|-android-view-normal-icon-image'),
('뷰탭 선택 아이콘', 'TabBarStyle-Main|-ios-view-selected-icon-image', 'TabBarStyle-Main|-android-view-selected-icon-image'),
('픽코마탭 아이콘', 'TabBarStyle-Main|-ios-piccoma-normal-icon-image', 'TabBarStyle-Main|-android-piccoma-normal-icon-image'),
('픽코마탭 선택 아이콘', 'TabBarStyle-Main|-ios-piccoma-selected-icon-image', 'TabBarStyle-Main|-android-piccoma-selected-icon-image'),
('쇼핑탭 아이콘', 'TabBarStyle-Main|-ios-shopping-normal-icon-image', 'TabBarStyle-Main|-android-shopping-normal-icon-image'),
('쇼핑탭 선택 아이콘', 'TabBarStyle-Main|-ios-shopping-selected-icon-image', 'TabBarStyle-Main|-android-shopping-selected-icon-image'),
('콜탭 아이콘', 'TabBarStyle-Main|-ios-call-normal-icon-image', 'TabBarStyle-Main|-android-call-normal-icon-image'),
('콜탭 선택 아이콘', 'TabBarStyle-Main|-ios-call-selected-icon-image', 'TabBarStyle-Main|-android-call-selected-icon-image'),
('더보기탭 아이콘', 'TabBarStyle-Main|-ios-more-normal-icon-image', 'TabBarStyle-Main|-android-more-normal-icon-image'),
('더보기탭 선택 아이콘', 'TabBarStyle-Main|-ios-more-selected-icon-image', 'TabBarStyle-Main|-android-more-selected-icon-image');

-- HeaderStyle-Main 선택자 (3개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('헤더 텍스트 컬러', 'HeaderStyle-Main|-ios-text-color', 'HeaderStyle-Main|-android-text-color'),
('탭 텍스트 컬러', 'HeaderStyle-Main|-ios-tab-text-color', 'HeaderStyle-Main|-android-tab-text-color'),
('탭 선택 텍스트 컬러', 'HeaderStyle-Main|-ios-tab-highlighted-text-color', 'HeaderStyle-Main|-android-tab-highlighted-text-color');

-- MainViewStyle-Primary 선택자 (10개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('메인뷰 배경 컬러', 'MainViewStyle-Primary|background-color', 'MainViewStyle-Primary|background-color'),
('메인뷰 배경 이미지', 'MainViewStyle-Primary|-ios-background-image', 'MainViewStyle-Primary|-android-background-image'),
('이름/타이틀 컬러', 'MainViewStyle-Primary|-ios-text-color', 'MainViewStyle-Primary|-android-text-color'),
('이름/타이틀 프레스 컬러', 'MainViewStyle-Primary|-ios-highlighted-text-color', 'MainViewStyle-Primary|-android-highlighted-text-color'),
('상태메시지 컬러', 'MainViewStyle-Primary|-ios-description-text-color', 'MainViewStyle-Primary|-android-description-text-color'),
('상태메시지 프레스 컬러', 'MainViewStyle-Primary|-ios-description-highlighted-text-color', 'MainViewStyle-Primary|-android-description-highlighted-text-color'),
('라스트메시지 컬러', 'MainViewStyle-Primary|-ios-paragraph-text-color', 'MainViewStyle-Primary|-android-paragraph-text-color'),
('라스트메시지 프레스 컬러', 'MainViewStyle-Primary|-ios-paragraph-highlighted-text-color', 'MainViewStyle-Primary|-android-paragraph-highlighted-text-color'),
('리스트 배경 컬러', 'MainViewStyle-Primary|-ios-normal-background-color', 'MainViewStyle-Primary|-android-normal-background-color'),
('리스트 배경 투명도', 'MainViewStyle-Primary|-ios-normal-background-alpha', 'MainViewStyle-Primary|-android-normal-background-alpha'),
('리스트 프레스 배경 컬러', 'MainViewStyle-Primary|-ios-selected-background-color', 'MainViewStyle-Primary|-android-selected-background-color'),
('리스트 프레스 배경 투명도', 'MainViewStyle-Primary|-ios-selected-background-alpha', 'MainViewStyle-Primary|-android-selected-background-alpha');

-- MainViewStyle-Secondary 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('3탭/4탭 배경 컬러', 'MainViewStyle-Secondary|background-color', 'MainViewStyle-Secondary|background-color');

-- SectionTitleStyle-Main 선택자 (4개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('섹션 보더 컬러', 'SectionTitleStyle-Main|border-color', 'SectionTitleStyle-Main|border-color'),
('섹션 보더 투명도', 'SectionTitleStyle-Main|border-alpha', 'SectionTitleStyle-Main|border-alpha'),
('섹션 타이틀 컬러', 'SectionTitleStyle-Main|-ios-text-color', 'SectionTitleStyle-Main|-android-text-color'),
('섹션 타이틀 투명도', 'SectionTitleStyle-Main|-ios-text-alpha', 'SectionTitleStyle-Main|-android-text-alpha');

-- FeatureStyle-Primary 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('서비스 버튼 컬러', 'FeatureStyle-Primary|-ios-text-color', 'FeatureStyle-Primary|-android-text-color');

-- ButtonStyle-AddFriend 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('친구추가 버튼 이미지', 'ButtonStyle-AddFriend|-ios-image', 'ButtonStyle-AddFriend|-android-image');

-- DefaultProfileStyle 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('기본 프로필 이미지', 'DefaultProfileStyle|-ios-profile-images', 'DefaultProfileStyle|-android-profile-images');

-- BackgroundStyle-ChatRoom 선택자 (2개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('채팅방 배경 컬러', 'BackgroundStyle-ChatRoom|background-color', 'BackgroundStyle-ChatRoom|background-color'),
('채팅방 배경 이미지', 'BackgroundStyle-ChatRoom|-ios-background-image', 'BackgroundStyle-ChatRoom|-android-background-image');

-- InputBarStyle-Chat 선택자 (9개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('인풋바 배경 컬러', 'InputBarStyle-Chat|background-color', 'InputBarStyle-Chat|background-color'),
('보내기 버튼 배경 컬러', 'InputBarStyle-Chat|-ios-send-normal-background-color', 'InputBarStyle-Chat|-android-send-normal-background-color'),
('보내기 버튼 아이콘 컬러', 'InputBarStyle-Chat|-ios-send-normal-foreground-color', 'InputBarStyle-Chat|-android-send-normal-foreground-color'),
('보내기 버튼 프레스 배경 컬러', 'InputBarStyle-Chat|-ios-send-highlighted-background-color', 'InputBarStyle-Chat|-android-send-highlighted-background-color'),
('보내기 버튼 프레스 아이콘 컬러', 'InputBarStyle-Chat|-ios-send-highlighted-foreground-color', 'InputBarStyle-Chat|-android-send-highlighted-foreground-color'),
('메뉴 버튼 아이콘 컬러', 'InputBarStyle-Chat|-ios-button-normal-foreground-color', 'InputBarStyle-Chat|-android-button-normal-foreground-color'),
('메뉴 버튼 프레스 아이콘 컬러', 'InputBarStyle-Chat|-ios-button-highlighted-foreground-color', 'InputBarStyle-Chat|-android-button-highlighted-foreground-color'),
('인풋바 텍스트 컬러', 'InputBarStyle-Chat|-ios-button-text-color', 'InputBarStyle-Chat|-android-button-text-color'),
('메뉴버튼/인풋바 배경 컬러', 'InputBarStyle-Chat|-ios-button-normal-background-color', 'InputBarStyle-Chat|-android-button-normal-background-color'),
('메뉴버튼/인풋바 배경 투명도', 'InputBarStyle-Chat|-ios-button-normal-background-alpha', 'InputBarStyle-Chat|-android-button-normal-background-alpha');

-- MessageCellStyle-Send 선택자 (9개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('보낸 메시지 첫번째 말풍선 이미지', 'MessageCellStyle-Send|-ios-background-image', 'MessageCellStyle-Send|-android-background-image'),
('보낸 메시지 첫번째 말풍선 선택 이미지', 'MessageCellStyle-Send|-ios-selected-background-image', 'MessageCellStyle-Send|-android-selected-background-image'),
('보낸 메시지 두번째 이상 말풍선 이미지', 'MessageCellStyle-Send|-ios-group-background-image', 'MessageCellStyle-Send|-android-group-background-image'),
('보낸 메시지 두번째 이상 말풍선 선택 이미지', 'MessageCellStyle-Send|-ios-group-selected-background-image', 'MessageCellStyle-Send|-android-group-selected-background-image'),
('보낸 메시지 첫번째 말풍선 인셋', 'MessageCellStyle-Send|-ios-title-edgeinsets', 'MessageCellStyle-Send|-android-title-edgeinsets'),
('보낸 메시지 두번째 이상 말풍선 인셋', 'MessageCellStyle-Send|-ios-group-title-edgeinsets', 'MessageCellStyle-Send|-android-group-title-edgeinsets'),
('보낸 메시지 텍스트 컬러', 'MessageCellStyle-Send|-ios-text-color', 'MessageCellStyle-Send|-android-text-color'),
('보낸 메시지 텍스트 선택 컬러', 'MessageCellStyle-Send|-ios-selected-text-color', 'MessageCellStyle-Send|-android-selected-text-color'),
('보낸 메시지 읽지않음 숫자 컬러', 'MessageCellStyle-Send|-ios-unread-text-color', 'MessageCellStyle-Send|-android-unread-text-color');

-- MessageCellStyle-Receive 선택자 (9개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('받은 메시지 첫번째 말풍선 이미지', 'MessageCellStyle-Receive|-ios-background-image', 'MessageCellStyle-Receive|-android-background-image'),
('받은 메시지 첫번째 말풍선 선택 이미지', 'MessageCellStyle-Receive|-ios-selected-background-image', 'MessageCellStyle-Receive|-android-selected-background-image'),
('받은 메시지 두번째 이상 말풍선 이미지', 'MessageCellStyle-Receive|-ios-group-background-image', 'MessageCellStyle-Receive|-android-group-background-image'),
('받은 메시지 두번째 이상 말풍선 선택 이미지', 'MessageCellStyle-Receive|-ios-group-selected-background-image', 'MessageCellStyle-Receive|-android-group-selected-background-image'),
('받은 메시지 첫번째 말풍선 인셋', 'MessageCellStyle-Receive|-ios-title-edgeinsets', 'MessageCellStyle-Receive|-android-title-edgeinsets'),
('받은 메시지 두번째 이상 말풍선 인셋', 'MessageCellStyle-Receive|-ios-group-title-edgeinsets', 'MessageCellStyle-Receive|-android-group-title-edgeinsets'),
('받은 메시지 텍스트 컬러', 'MessageCellStyle-Receive|-ios-text-color', 'MessageCellStyle-Receive|-android-text-color'),
('받은 메시지 텍스트 선택 컬러', 'MessageCellStyle-Receive|-ios-selected-text-color', 'MessageCellStyle-Receive|-android-selected-text-color'),
('받은 메시지 읽지않음 숫자 컬러', 'MessageCellStyle-Receive|-ios-unread-text-color', 'MessageCellStyle-Receive|-android-unread-text-color');

-- BackgroundStyle-Passcode 선택자 (2개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('잠금화면 배경 컬러', 'BackgroundStyle-Passcode|background-color', 'BackgroundStyle-Passcode|background-color'),
('잠금화면 배경 이미지', 'BackgroundStyle-Passcode|-ios-background-image', 'BackgroundStyle-Passcode|-android-background-image');

-- LabelStyle-PasscodeTitle 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('잠금화면 텍스트 컬러', 'LabelStyle-PasscodeTitle|-ios-text-color', 'LabelStyle-PasscodeTitle|-android-text-color');

-- PasscodeStyle 선택자 (11개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('키패드 첫번째 불릿 이미지', 'PasscodeStyle|-ios-bullet-first-image', 'PasscodeStyle|-android-bullet-first-image'),
('키패드 두번째 불릿 이미지', 'PasscodeStyle|-ios-bullet-second-image', 'PasscodeStyle|-android-bullet-second-image'),
('키패드 세번째 불릿 이미지', 'PasscodeStyle|-ios-bullet-third-image', 'PasscodeStyle|-android-bullet-third-image'),
('키패드 네번째 불릿 이미지', 'PasscodeStyle|-ios-bullet-fourth-image', 'PasscodeStyle|-android-bullet-fourth-image'),
('키패드 첫번째 불릿 선택 이미지', 'PasscodeStyle|-ios-bullet-selected-first-image', 'PasscodeStyle|-android-bullet-selected-first-image'),
('키패드 두번째 불릿 선택 이미지', 'PasscodeStyle|-ios-bullet-selected-second-image', 'PasscodeStyle|-android-bullet-selected-second-image'),
('키패드 세번째 불릿 선택 이미지', 'PasscodeStyle|-ios-bullet-selected-third-image', 'PasscodeStyle|-android-bullet-selected-third-image'),
('키패드 네번째 불릿 선택 이미지', 'PasscodeStyle|-ios-bullet-selected-fourth-image', 'PasscodeStyle|-android-bullet-selected-fourth-image'),
('키패드 배경 컬러', 'PasscodeStyle|-ios-keypad-background-color', 'PasscodeStyle|-android-keypad-background-color'),
('키패드 숫자 텍스트 컬러', 'PasscodeStyle|-ios-keypad-text-normal-color', 'PasscodeStyle|-android-keypad-text-normal-color'),
('키패드 프레스 이미지', 'PasscodeStyle|-ios-keypad-number-highlighted-image', 'PasscodeStyle|-android-keypad-number-highlighted-image');

-- BackgroundStyle-MessageNotificationBar 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('메시지 알림 배너 배경 컬러', 'BackgroundStyle-MessageNotificationBar|background-color', 'BackgroundStyle-MessageNotificationBar|background-color');

-- LabelStyle-MessageNotificationBarName 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('메시지 알림 배너 이름 컬러', 'LabelStyle-MessageNotificationBarName|-ios-text-color', 'LabelStyle-MessageNotificationBarName|-android-text-color');

-- LabelStyle-MessageNotificationBarMessage 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('메시지 알림 배너 텍스트 컬러', 'LabelStyle-MessageNotificationBarMessage|-ios-text-color', 'LabelStyle-MessageNotificationBarMessage|-android-text-color');

-- BackgroundStyle-DirectShareBar 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('전달 완료 배너 배경 컬러', 'BackgroundStyle-DirectShareBar|background-color', 'BackgroundStyle-DirectShareBar|background-color');

-- LabelStyle-DirectShareBarName 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('전달 완료 배너 이름 컬러', 'LabelStyle-DirectShareBarName|-ios-text-color', 'LabelStyle-DirectShareBarName|-android-text-color');

-- LabelStyle-DirectShareBarMessage 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('전달 완료 배너 텍스트 컬러', 'LabelStyle-DirectShareBarMessage|-ios-text-color', 'LabelStyle-DirectShareBarMessage|-android-text-color');

-- BottomBannerStyle 선택자 (1개 속성)
INSERT INTO color_style (explain, ios_style_name, android_style_name) VALUES
('탭 배너 배경 컬러', 'BottomBannerStyle|background-color', 'BottomBannerStyle|background-color');

-- 총 개수 확인
-- 선택자: 18개
-- 속성: 94개 (ManifestStyle 5 + TabBarStyle-Main 17 + HeaderStyle-Main 3 + MainViewStyle-Primary 12 + MainViewStyle-Secondary 1 + SectionTitleStyle-Main 4 + FeatureStyle-Primary 1 + ButtonStyle-AddFriend 1 + DefaultProfileStyle 1 + BackgroundStyle-ChatRoom 2 + InputBarStyle-Chat 10 + MessageCellStyle-Send 9 + MessageCellStyle-Receive 9 + BackgroundStyle-Passcode 2 + LabelStyle-PasscodeTitle 1 + PasscodeStyle 11 + BackgroundStyle-MessageNotificationBar 1 + LabelStyle-MessageNotificationBarName 1 + LabelStyle-MessageNotificationBarMessage 1 + BackgroundStyle-DirectShareBar 1 + LabelStyle-DirectShareBarName 1 + LabelStyle-DirectShareBarMessage 1 + BottomBannerStyle 1)

-- 데이터 개수 확인 쿼리
SELECT COUNT(*) as total_count FROM color_style;

-- 카테고리별 개수 확인 쿼리
SELECT 
  CASE 
    WHEN ios_style_name LIKE 'ManifestStyle%' THEN 'Manifest'
    WHEN ios_style_name LIKE 'TabBarStyle%' THEN 'TabBar'
    WHEN ios_style_name LIKE 'HeaderStyle%' THEN 'Header'
    WHEN ios_style_name LIKE 'MainViewStyle%' THEN 'MainView'
    WHEN ios_style_name LIKE 'SectionTitleStyle%' THEN 'Section'
    WHEN ios_style_name LIKE 'FeatureStyle%' THEN 'Feature'
    WHEN ios_style_name LIKE 'ButtonStyle%' THEN 'Button'
    WHEN ios_style_name LIKE 'DefaultProfileStyle%' THEN 'DefaultProfile'
    WHEN ios_style_name LIKE '%ChatRoom%' OR ios_style_name LIKE 'InputBarStyle%' THEN 'ChatRoom'
    WHEN ios_style_name LIKE 'MessageCellStyle%' THEN 'Message'
    WHEN ios_style_name LIKE '%Passcode%' THEN 'Passcode'
    WHEN ios_style_name LIKE '%MessageNotification%' THEN 'Notification'
    WHEN ios_style_name LIKE '%DirectShare%' THEN 'DirectShare'
    WHEN ios_style_name LIKE 'BottomBannerStyle%' THEN 'BottomBanner'
    ELSE '기타'
  END as category,
  COUNT(*) as count
FROM color_style
GROUP BY category
ORDER BY category;