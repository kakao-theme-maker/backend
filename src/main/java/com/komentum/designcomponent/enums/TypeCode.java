package com.komentum.designcomponent.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum TypeCode {

  // main view images
  MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE("mainviewStylePrimaryBackgroundImage"),

  // tab bar images
  TABBAR_STYLE_BACKGROUND_IMAGE("tabbarStyleBackgroundImage"),
  TABBAR_STYLE_FRIENDS_NORMAL_ICON_IMAGE("tabbarStyleFriendsNormalIconImage"),
  TABBAR_STYLE_FRIENDS_SELECTED_ICON_IMAGE("tabbarStyleFriendsSelectedIconImage"),
  TABBAR_STYLE_CHATS_NORMAL_ICON_IMAGE("tabbarStyleChatsNormalIconImage"),
  TABBAR_STYLE_CHATS_SELECTED_ICON_IMAGE("tabbarStyleChatsSelectedIconImage"),
  TABBAR_STYLE_OPEN_CHATS_NORMAL_ICON_IMAGE("tabbarStyleOpenChatsNormalIconImage"),
  TABBAR_STYLE_OPEN_CHATS_SELECTED_ICON_IMAGE("tabbarStyleOpenChatsSelectedIconImage"),
  TABBAR_STYLE_SHOPPING_NORMAL_ICON_IMAGE("tabbarStyleShoppingNormalIconImage"),
  TABBAR_STYLE_SHOPPING_SELECTED_ICON_IMAGE("tabbarStyleShoppingSelectedIconImage"),
  TABBAR_STYLE_MORE_NORMAL_ICON_IMAGE("tabbarStyleMoreNormalIconImage"),
  TABBAR_STYLE_MORE_SELECTED_ICON_IMAGE("tabbarStyleMoreSelectedIconImage"),

  // passcode page images
  PASSCODE_BACKGROUND_IMAGE("passcodeBackgroundImage"),
  PASSCODE_STYLE_BULLET_FIRST_IMAGE("passcodeStyleBulletFirstImage"),
  PASSCODE_STYLE_BULLET_SECOND_IMAGE("passcodeStyleBulletSecondImage"),
  PASSCODE_STYLE_BULLET_THIRD_IMAGE("passcodeStyleBulletThirdImage"),
  PASSCODE_STYLE_BULLET_FOURTH_IMAGE("passcodeStyleBulletFourthImage"),
  PASSCODE_STYLE_BULLET_SELECTED_FIRST_IMAGE("passcodeStyleBulletSelectedFirstImage"),
  PASSCODE_STYLE_BULLET_SELECTED_SECOND_IMAGE("passcodeStyleBulletSelectedSecondImage"),
  PASSCODE_STYLE_BULLET_SELECTED_THIRD_IMAGE("passcodeStyleBulletSelectedThirdImage"),
  PASSCODE_STYLE_BULLET_SELECTED_FOURTH_IMAGE("passcodeStyleBulletSelectedFourthImage"),

  // chat room images
  CHAT_ROOM_BACKGROUND_IMAGE("chatRoomBackgroundImage"),
  MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE("messageCellStyleSendBackgroundImage"),
  MESSAGE_CELL_STYLE_SEND_GROUP_BACKGROUND_IMAGE("messageCellStyleSendGroupBackgroundImage"),
  MESSAGE_CELL_STYLE_RECEIVE_BACKGROUND_IMAGE("messageCellStyleReceiveBackgroundImage"),
  MESSAGE_CELL_STYLE_RECEIVE_GROUP_BACKGROUND_IMAGE("messageCellStyleReceiveGroupBackgroundImage"),

  // profile style
  DEFAULT_PROFILE_STYLE_PROFILE_IMAGE("defaultProfileStyleProfileImage"),

  // button style
  BUTTON_STYLE_ADD_FRIEND_IMAGE("buttonStyleAddFriendImage"),

  // theme icon
  COMMON_ICO_THEME("commonIcoTheme");

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
        .orElseThrow(
            () -> new IllegalArgumentException("Unsupported type code : " + typeCodeString));
  }
}
