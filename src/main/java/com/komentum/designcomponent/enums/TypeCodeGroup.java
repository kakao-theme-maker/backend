package com.komentum.designcomponent.enums;

import lombok.Getter;

@Getter
public enum TypeCodeGroup {
  DEFAULT_PROFILE("기본 프로필"),
  MAINVIEW_BACKGROUND("메인 배경"),
  TABBAR_BACKGROUND("탭 배경"),
  TABBAR_ICON("탭 아이콘"),
  PASSCODE_BACKGROUND("잠금 배경"),
  PASSCODE_BULLET("잠금 불릿"),
  CHATROOM_BACKGROUND("채팅방 배경"),
  CHATROOM_BUBBLE("채팅방 말풍선"),
  SERVICE_BUTTON("서비스 버튼"),
  COMMON_THEME_ICON("테마 아이콘");

  private final String description;

  TypeCodeGroup(String description) {
    this.description = description;
  }
}
