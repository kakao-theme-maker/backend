package com.komentum.test.fixture.component;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.user.domain.User;

public final class DesignComponentFixture {

  private DesignComponentFixture() {
  }

  /**
   * 지정한 소유자와 파일명을 가진 공개 미저장 에셋을 생성한다.
   */
  public static DesignComponent designComponent(User owner, String fileName) {
    return designComponent(owner, fileName, true);
  }

  /**
   * 지정한 소유자·파일명·공개 여부를 가진 미저장 에셋을 생성한다.
   */
  public static DesignComponent designComponent(User owner, String fileName, Boolean isPublic) {
    return create(null, owner, fileName, isPublic);
  }

  /**
   * 매퍼 단위 테스트에서 사용할 ID가 지정된 에셋을 생성한다.
   */
  public static DesignComponent designComponent(int id) {
    return create(id, null, null, true);
  }

  /**
   * ID 유무와 관계없이 동일한 생성 경로로 에셋을 구성한다.
   */
  private static DesignComponent create(Integer id, User owner, String fileName, Boolean isPublic) {
    return DesignComponent.builder()
        .designComponentId(id)
        .user(owner)
        .fileName(fileName)
        .isPublic(isPublic)
        .build();
  }
}
