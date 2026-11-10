package com.komentum.test.fixture.component;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;

public final class ComponentTypeFixture {

  private ComponentTypeFixture() {
  }

  /**
   * 타입 코드와 기본 이름을 가진 미저장 컴포넌트 타입을 생성한다.
   */
  public static ComponentType componentType(TypeCode typeCode) {
    return create(null, typeCode);
  }

  /**
   * 매퍼 단위 테스트에서 사용할 ID가 지정된 컴포넌트 타입을 생성한다.
   */
  public static ComponentType componentType(int id, TypeCode typeCode) {
    return create(id, typeCode);
  }

  /**
   * ID 유무와 관계없이 동일한 기본값으로 컴포넌트 타입을 생성한다.
   */
  private static ComponentType create(Integer id, TypeCode typeCode) {
    return ComponentType.builder()
        .componentTypeId(id)
        .typeCode(typeCode)
        .name(typeCode.name())
        .build();
  }
}
