package com.komentum.theme.component.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum TypeCode {
  // theme preview image
  COMMON_ICO_THEME("commonIcoTheme"),
  // friend / chat tab background image
  BACKGROUND_IMAGE("backgroundImage"),
  // main tab images
  MAIN_TAB_BG_IMAGE("maintabBgImage"),
  MAIN_TAB_ICO_FRIENDS("maintabIcoFriends"),
  MAIN_TAB_ICO_FRIENDS_SELECTED("maintabIcoFriendsSelected"),
  MAIN_TAB_ICO_CHAT("maintabIcoChat"),
  MAIN_TAB_ICO_CHAT_SELECTED("maintabIcoChatSelected"),
  MAIN_TAB_ICO_OPENCHAT("maintabIcoOpenchat"),
  MAIN_TAB_ICO_OPENCHAT_SELECTED("maintabIcoOpenchatSelected"),
  MAIN_TAB_ICO_MORE("maintabIcoMore"),
  MAIN_TAB_ICO_MORE_SELECTED("maintabIcoMoreSelected"),
  MAIN_TAB_ICO_SHOPPING("maintabIcoShopping"),
  MAIN_TAB_ICO_SHOPPING_SELECTED("maintabIcoShoppingSelected"),
  FIND_BTN_ADD_FRIEND("findBtnAddFriend"),
  FIND_BTN_ADD_FRIEND_SELECTED("findBtnAddFriendSelected"),
  // user default profile image
  DEFAULT_PROFILE_IMG("defaultProfileImg"),
  // chat room images
  CHAT_ROOM_BG_IMAGE("chatRoomBgImage"),
  SENDER_FIRST_BUBBLE_IMAGE("senderFirstBubbleImage"),
  SENDER_FIRST_BUBBLE_IMAGE_SELECTED("senderFirstBubbleImageSelected"),
  SENDER_MIDDLE_BUBBLE_IMAGE("senderMiddleBubbleImage"),
  SENDER_MIDDLE_BUBBLE_IMAGE_SELECTED("senderMiddleBubbleImageSelected"),
  RECEIVER_FIRST_BUBBLE_IMAGE("receiverFirstBubbleImage"),
  RECEIVER_FIRST_BUBBLE_IMAGE_SELECTED("receiverFirstBubbleImageSelected"),
  RECEIVER_MIDDLE_BUBBLE_IMAGE("receiverMiddleBubbleImage"),
  RECEIVER_MIDDLE_BUBBLE_IMAGE_SELECTED("receiverMiddleBubbleImageSelected"),
  // pass code page images
  PASS_CODE_BG_IMAGE("passCodeBgImage"),
  PASS_CODE_INPUT_IMAGE_EMPTY_1("passCodeInputImageEmpty1"),
  PASS_CODE_INPUT_IMAGE_EMPTY_2("passCodeInputImageEmpty2"),
  PASS_CODE_INPUT_IMAGE_EMPTY_3("passCodeInputImageEmpty3"),
  PASS_CODE_INPUT_IMAGE_EMPTY_4("passCodeInputImageEmpty4"),
  PASS_CODE_INPUT_IMAGE_FILLED_1("passCodeInputImageFilled1"),
  PASS_CODE_INPUT_IMAGE_FILLED_2("passCodeInputImageFilled2"),
  PASS_CODE_INPUT_IMAGE_FILLED_3("passCodeInputImageFilled3"),
  PASS_CODE_INPUT_IMAGE_FILLED_4("passCodeInputImageFilled4"),
  PASS_CODE_KEYPAD_PRESS_IMAGE("passCodeKeypadPressImage");

  private final String typeCode;

  TypeCode(String typeCode) {
    this.typeCode = typeCode;
  }

  /**
   * 요청, 응답 모두 typeCode String을 사용한다
   * 사유 : FE, BE가 공유하는 문서의 typeCode key 값이 서버 내 typeCode String이기 때문
   * */
  @JsonValue
  public String getTypeCode() {
    return typeCode;
  }

  public static TypeCode from(String typeCodeString) {
    return Arrays.stream(values())
        .filter(typeCode -> typeCode.typeCode.equals(typeCodeString))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported type code"));
  }
}
