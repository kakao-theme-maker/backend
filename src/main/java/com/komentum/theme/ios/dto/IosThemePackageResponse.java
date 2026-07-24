package com.komentum.theme.ios.dto;

import com.komentum.designcomponent.enums.Platform;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IosThemePackageResponse {

  private Integer themeComponentId;
  private Platform platform;
  private String fileName;
  private String themeUrl;
}
