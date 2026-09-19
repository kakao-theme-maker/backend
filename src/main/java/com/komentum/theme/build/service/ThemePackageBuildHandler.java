package com.komentum.theme.build.service;

import com.komentum.designcomponent.enums.Platform;

public interface ThemePackageBuildHandler {

  Platform platform();

  /**
   * 테마 패키지를 빌드하여 외부 파일 저장소에 업로드하고, 업로드된 파일의 파일명(확장자 포함)을 반환한다.
   * */
  String build(Integer themeComponentId);
}
