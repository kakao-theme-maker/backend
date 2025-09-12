package com.komentum.theme.component.enums;

public enum Platform {
  ANDROID,
  IOS;

  public static Platform fromString(String platform) {
    for (Platform platformEnum : Platform.values()) {
      if (platformEnum.name().equalsIgnoreCase(platform)) {
        return platformEnum;
      }
    }
    throw new IllegalArgumentException("Invalid platform: " + platform);
  }
}
