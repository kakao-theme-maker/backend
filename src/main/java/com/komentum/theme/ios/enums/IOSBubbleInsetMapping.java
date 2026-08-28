package com.komentum.theme.ios.enums;

import com.komentum.designcomponent.enums.TypeCode;
import java.util.List;
import lombok.Getter;

@Getter
public enum IOSBubbleInsetMapping {
  MESSAGE_CELL_SEND_BACKGROUND_IMAGE(
      TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE,
      "MessageCellStyle-Send",
      List.of("-ios-background-image", "-ios-selected-background-image", "-ios-title-edgeinsets")
  ),
  MESSAGE_CELL_SEND_GROUP_BACKGROUND_IMAGE(
      TypeCode.MESSAGE_CELL_STYLE_SEND_GROUP_BACKGROUND_IMAGE,
      "MessageCellStyle-Send",
      List.of("-ios-group-background-image", "-ios-group-selected-background-image",
          "-ios-group-title-edgeinsets")
  ),
  MESSAGE_CELL_RECEIVE_BACKGROUND_IMAGE(
      TypeCode.MESSAGE_CELL_STYLE_RECEIVE_BACKGROUND_IMAGE,
      "MessageCellStyle-Receive",
      List.of("-ios-background-image", "-ios-selected-background-image", "-ios-title-edgeinsets")
  ),
  MESSAGE_CELL_RECEIVE_GROUP_BACKGROUND_IMAGE(
      TypeCode.MESSAGE_CELL_STYLE_RECEIVE_GROUP_BACKGROUND_IMAGE,
      "MessageCellStyle-Receive",
      List.of("-ios-group-background-image", "-ios-group-selected-background-image",
          "-ios-group-title-edgeinsets")
  );

  private final TypeCode typeCode;
  private final String selector;
  private final List<String> properties;

  IOSBubbleInsetMapping(TypeCode typeCode, String selector, List<String> properties) {
    this.typeCode = typeCode;
    this.selector = selector;
    this.properties = properties;
  }

  public static IOSBubbleInsetMapping from(TypeCode typeCode) {
    for (IOSBubbleInsetMapping mapping : IOSBubbleInsetMapping.values()) {
      if (mapping.typeCode == typeCode) {
        return mapping;
      }
    }
    throw new IllegalArgumentException(
        "[IOSBubbleInsetMapping] Unsupported type code : " + typeCode);
  }
}
