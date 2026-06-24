package com.komentum.designcomponent.enums;

import lombok.Getter;

@Getter
public enum Platform {
  ANDROID("android"),
  IOS("ios");

  private final String platformName;

  Platform(String platformName) {
    this.platformName = platformName;
  }

  public static Platform fromString(String platform) {
    for (Platform platformEnum : Platform.values()) {
      if (platformEnum.name().equalsIgnoreCase(platform)) {
        return platformEnum;
      }
    }
    throw new IllegalArgumentException("Invalid platform: " + platform);
  }
}
