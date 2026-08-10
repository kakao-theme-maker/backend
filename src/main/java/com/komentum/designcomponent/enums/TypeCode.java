package com.komentum.designcomponent.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum TypeCode {

  // mainview background
  MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE("mainviewStylePrimaryBackgroundImage",
      TypeCodeGroup.MAINVIEW_BACKGROUND),

  // tabbar background
  TABBAR_STYLE_BACKGROUND_IMAGE("tabbarStyleBackgroundImage",
      TypeCodeGroup.TABBAR_BACKGROUND),

  // tabbar icon
  TABBAR_STYLE_FRIENDS_NORMAL_ICON_IMAGE("tabbarStyleFriendsNormalIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_FRIENDS_SELECTED_ICON_IMAGE("tabbarStyleFriendsSelectedIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_CHATS_NORMAL_ICON_IMAGE("tabbarStyleChatsNormalIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_CHATS_SELECTED_ICON_IMAGE("tabbarStyleChatsSelectedIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_OPEN_CHATS_NORMAL_ICON_IMAGE("tabbarStyleOpenChatsNormalIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_OPEN_CHATS_SELECTED_ICON_IMAGE("tabbarStyleOpenChatsSelectedIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_SHOPPING_NORMAL_ICON_IMAGE("tabbarStyleShoppingNormalIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_SHOPPING_SELECTED_ICON_IMAGE("tabbarStyleShoppingSelectedIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_MORE_NORMAL_ICON_IMAGE("tabbarStyleMoreNormalIconImage",
      TypeCodeGroup.TABBAR_ICON),
  TABBAR_STYLE_MORE_SELECTED_ICON_IMAGE("tabbarStyleMoreSelectedIconImage",
      TypeCodeGroup.TABBAR_ICON),

  // passcode background
  PASSCODE_BACKGROUND_IMAGE("passcodeBackgroundImage",
      TypeCodeGroup.PASSCODE_BACKGROUND),

  // passcode bullet
  PASSCODE_STYLE_BULLET_FIRST_IMAGE("passcodeStyleBulletFirstImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_SECOND_IMAGE("passcodeStyleBulletSecondImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_THIRD_IMAGE("passcodeStyleBulletThirdImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_FOURTH_IMAGE("passcodeStyleBulletFourthImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_SELECTED_FIRST_IMAGE("passcodeStyleBulletSelectedFirstImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_SELECTED_SECOND_IMAGE("passcodeStyleBulletSelectedSecondImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_SELECTED_THIRD_IMAGE("passcodeStyleBulletSelectedThirdImage",
      TypeCodeGroup.PASSCODE_BULLET),
  PASSCODE_STYLE_BULLET_SELECTED_FOURTH_IMAGE("passcodeStyleBulletSelectedFourthImage",
      TypeCodeGroup.PASSCODE_BULLET),

  // chat room background
  CHAT_ROOM_BACKGROUND_IMAGE("chatRoomBackgroundImage",
      TypeCodeGroup.CHATROOM_BACKGROUND),

  // chatroom bubble
  MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE("messageCellStyleSendBackgroundImage",
      TypeCodeGroup.CHATROOM_BUBBLE),
  MESSAGE_CELL_STYLE_SEND_GROUP_BACKGROUND_IMAGE("messageCellStyleSendGroupBackgroundImage",
      TypeCodeGroup.CHATROOM_BUBBLE),
  MESSAGE_CELL_STYLE_RECEIVE_BACKGROUND_IMAGE("messageCellStyleReceiveBackgroundImage",
      TypeCodeGroup.CHATROOM_BUBBLE),
  MESSAGE_CELL_STYLE_RECEIVE_GROUP_BACKGROUND_IMAGE("messageCellStyleReceiveGroupBackgroundImage",
      TypeCodeGroup.CHATROOM_BUBBLE),

  // default profile
  DEFAULT_PROFILE_STYLE_PROFILE_IMAGE("defaultProfileStyleProfileImage",
      TypeCodeGroup.DEFAULT_PROFILE),

  // service button
  BUTTON_STYLE_ADD_FRIEND_IMAGE("buttonStyleAddFriendImage",
      TypeCodeGroup.SERVICE_BUTTON),
  ;

  private final String typeCode;
  private final TypeCodeGroup typeCodeGroup;

  TypeCode(String typeCode, TypeCodeGroup typeCodeGroup) {
    this.typeCode = typeCode;
    this.typeCodeGroup = typeCodeGroup;
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
