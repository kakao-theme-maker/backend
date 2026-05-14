package com.komentum.theme.component.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum StyleCode {
  // main tab colors
  MAIN_TAB_BG_COLOR("maintabBgColor"),
  // main view colors
  HEADER_TITLE_ICON_COLOR("headerTitleIconColor"),
  BACKGROUND_COLOR("backgroundColor"),
  NAME_TITLE_COLOR("nameTitleColor"),
  NAME_TITLE_COLOR_SELECTED("nameTitleColorSelected"),
  STATUS_MESSAGE_COLOR("statusMessageColor"),
  STATUS_MESSAGE_COLOR_SELECTED("statusMessageColorSelected"),
  LAST_MESSAGE_COLOR("lastMessageColor"),
  LAST_MESSAGE_COLOR_SELECTED("lastMessageColorSelected"),
  LIST_BACKGROUND_COLOR("listBackgroundColor"),
  LIST_BACKGROUND_COLOR_SELECTED("listBackgroundColorSelected"),
  BORDER_COLOR("borderColor"),
  SECTION_HEADER_COLOR("sectionHeaderColor"),
  SERVICE_BUTTON_COLOR("serviceButtonColor"),
  // openchat / shopping tab colors
  BACKGROUND_COLOR_SECONDARY("backgroundColorSecondary"),
  // chatroom colors
  CHATROOM_BACKGROUND_COLOR("chatRoomBackgroundColor"),
  INPUT_BAR_BACKGROUND_COLOR("inputBarBackgroundColor"),
  INPUT_BAR_TEXT_COLOR("inputBarTextColor"),
  SEND_BUTTON_BACKGROUND_COLOR("sendButtonBackgroundColor"),
  SEND_BUTTON_BACKGROUND_COLOR_SELECTED("sendButtonBackgroundColorSelected"),
  SEND_BUTTON_ICON_COLOR("sendButtonIconColor"),
  SEND_BUTTON_ICON_COLOR_SELECTED("sendButtonIconColorSelected"),
  CHAT_ROOM_MENU_ICON_COLOR("chatRoomMenuIconColor"),
  CHAT_ROOM_MENU_ICON_COLOR_SELECTED("chatRoomMenuIconColorSelected"),
  CHAT_ROOM_MENU_INPUT_BAR_BACKGROUND_COLOR("chatRoomMenuInputBarBackgroundColor"),
  SENDER_BUBBLE_TEXT_COLOR("senderBubbleTextColor"),
  SENDER_BUBBLE_TEXT_COLOR_SELECTED("senderBubbleTextColorSelected"),
  SENDER_UNREAD_COUNT_TEXT_COLOR("senderUnreadCountTextColor"),
  RECEIVER_BUBBLE_TEXT_COLOR("receiverBubbleTextColor"),
  RECEIVER_BUBBLE_TEXT_COLOR_SELECTED("receiverBubbleTextColorSelected"),
  RECEIVER_UNREAD_COUNT_TEXT_COLOR("receiverUnreadCountTextColor"),
  // pass code page colors
  PASS_CODE_BACKGROUND_COLOR("passCodeBackgroundColor"),
  PASS_CODE_TITLE_COLOR("passCodeTitleColor"),
  PASS_CODE_KEYPAD_BACKGROUND_COLOR("passCodeKeypadBackgroundColor"),
  PASS_CODE_KEYPAD_TEXT_COLOR("passCodeKeypadTextColor"),
  PASS_CODE_KEYPAD_TEXT_COLOR_SELECTED("passCodeKeypadTextColorSelected"),
  PASS_CODE_KEYPAD_PRESS_COLOR("passCodeKeypadPressColor"),
  // banner colors
  MESSAGE_NOTIFICATION_BACKGROUND_COLOR("messageNotificationBackgroundColor"),
  MESSAGE_NOTIFICATION_TITLE_COLOR("messageNotificationTitleColor"),
  MESSAGE_NOTIFICATION_MESSAGE_COLOR("messageNotificationMessageColor"),
  MESSAGE_DIRECT_SHARE_BACKGROUND_COLOR("messageDirectShareBackgroundColor"),
  MESSAGE_DIRECT_SHARE_TITLE_COLOR("messageDirectShareTitleColor"),
  MESSAGE_DIRECT_SHARE_MESSAGE_COLOR("messageDirectShareMessageColor"),
  TAB_BOTTOM_BANNER_COLOR("tabBottomBannerColor"),
  ;

  private final String styleCode;

  StyleCode(String styleCode) {
    this.styleCode = styleCode;
  }

  /**
   * 요청, 응답 모두 styleCode String을 사용한다
   * 사유 : FE, BE가 공유하는 문서의 styleCode key 값이 서버 내 styleCode String이기 때문
   * */
  @JsonValue
  public String getStyleCode() {
    return styleCode;
  }

  public static StyleCode from(String styleCodeString) {
    return Arrays.stream(values())
        .filter(styleCode -> styleCode.styleCode.equals(styleCodeString))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("Unsupported style code: " + styleCodeString));
  }
}
