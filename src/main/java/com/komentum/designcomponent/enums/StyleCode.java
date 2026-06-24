package com.komentum.designcomponent.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum StyleCode {
  // main view style
  MAINVIEW_STYLE_BACKGROUND_COLOR("mainviewStyleBackgroundColor"),
  MAINVIEW_STYLE_TEXT_COLOR("mainviewStyleTextColor"),
  MAINVIEW_STYLE_HIGHLIGHTED_TEXT_COLOR("mainviewStyleHighlightedTextColor"),
  MAINVIEW_STYLE_DESCRIPTION_TEXT_COLOR("mainviewStyleDescriptionTextColor"),
  MAINVIEW_STYLE_DESCRIPTION_HIGHLIGHTED_TEXT_COLOR("mainviewStyleDescriptionHighlightedTextColor"),
  MAINVIEW_STYLE_NORMAL_BACKGROUND_COLOR("mainviewStyleNormalBackgroundColor"),
  MAINVIEW_STYLE_SELECTED_BACKGROUND_COLOR("mainviewStyleSelectedBackgroundColor"),
  MAINVIEW_STYLE_PARAGRAPH_TEXT_COLOR("mainviewStyleParagraphTextColor"),
  MAINVIEW_STYLE_PARAGRAPH_HIGHLIGHTED_TEXT_COLOR("mainviewStyleParagraphHighlightedTextColor"),
  // feature button style
  FEATURE_STYLE_TEXT_COLOR("featureStyleTextColor"),
  // main view style - secondary
  MAINVIEW_STYLE_SECONDARY_BACKGROUND_COLOR("mainviewStyleSecondaryBackgroundColor"),
  // header style
  HEADER_STYLE_TEXT_COLOR("headerStyleTextColor"),
  HEADER_STYLE_TAB_TEXT_COLOR("headerStyleTabTextColor"),
  // section style
  SECTION_TITLE_STYLE_TEXT_COLOR("sectionTitleStyleTextColor"),
  SECTION_TITLE_STYLE_BORDER_COLOR("sectionTitleStyleBorderColor"),
  // tabbar style
  TABBAR_STYLE_BACKGROUND_COLOR("tabbarStyleBackgroundColor"),
  // message notification bar style
  MESSAGE_NOTIFICATION_BAR_BACKGROUND_COLOR("messageNotificationBarBackgroundColor"),
  MESSAGE_NOTIFICATION_BAR_NAME_TEXT_COLOR("messageNotificationBarNameTextColor"),
  MESSAGE_NOTIFICATION_BAR_MESSAGE_TEXT_COLOR("messageNotificationBarMessageTextColor"),
  // direct share bar style
  DIRECT_SHARE_BAR_BACKGROUND_COLOR("directShareBarBackgroundColor"),
  DIRECT_SHARE_BAR_NAME_TEXT_COLOR("directShareBarNameTextColor"),
  DIRECT_SHARE_BAR_MESSAGE_TEXT_COLOR("directShareBarMessageTextColor"),
  // passcode page style
  PASSCODE_BACKGROUND_COLOR("passcodeBackgroundColor"),
  PASSCODE_STYLE_KEYPAD_BACKGROUND_COLOR("passcodeStyleKeypadBackgroundColor"),
  PASSCODE_STYLE_KEYPAD_TEXT_NORMAL_COLOR("passcodeStyleKeypadTextNormalColor"),
  // chat room style
  CHAT_ROOM_BACKGROUND_COLOR("chatRoomBackgroundColor"),
  MESSAGE_CELL_STYLE_SEND_TEXT_COLOR("messageCellStyleSendTextColor"),
  MESSAGE_CELL_STYLE_RECEIVE_TEXT_COLOR("messageCellStyleReceiveTextColor"),
  MESSAGE_CELL_STYLE_UNREAD_COUNT_COLOR("messageCellStyleUnreadCountColor"),
  INPUT_BAR_STYLE_CHAT_BUTTON_TEXT_COLOR("inputBarStyleChatButtonTextColor"),
  INPUT_BAR_STYLE_CHAT_BACKGROUND_COLOR("inputBarStyleChatBackgroundColor"),
  INPUT_BAR_STYLE_CHAT_BUTTON_NORMAL_FOREGROUND_COLOR(
      "inputBarStyleChatButtonNormalForegroundColor"),
  INPUT_BAR_STYLE_CHAT_BUTTON_NORMAL_BACKGROUND_COLOR(
      "inputBarStyleChatButtonNormalBackgroundColor"),
  INPUT_BAR_STYLE_CHAT_SEND_NORMAL_BACKGROUND_COLOR("inputBarStyleChatSendNormalBackgroundColor"),
  INPUT_BAR_STYLE_CHAT_SEND_NORMAL_FOREGROUND_COLOR("inputBarStyleChatSendNormalForegroundColor");

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
