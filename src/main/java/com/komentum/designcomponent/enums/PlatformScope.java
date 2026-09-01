package com.komentum.designcomponent.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/**
 * 플랫폼 적용 범위를 나타낸다.
 *
 * <p>
 * {@link Platform}은 어떤 플랫폼에 속하는지를 나타내고,
 * PlatformScope는 해당 요소가 모든 플랫폼에서 공통으로 사용되는지
 * 특정 플랫폼에 종속되는지를 나타낸다.
 * </p>
 *
 * <p>
 * Platform과 별도로 존재하는 이유는 공통 여부와 대상 플랫폼이 서로 다른 의미를 가지기 때문이다.
 * 공통 요소를 Platform.COMMON으로 표현하면 플랫폼을 나타내는 {@link Platform}의 의미가 혼재될 수 있다.
 * </p>
 */
public enum PlatformScope {
  COMMON("common"),
  ANDROID("android"),
  IOS("ios");

  private final String scopeName;

  PlatformScope(String scopeName) {
    this.scopeName = scopeName;
  }

  @JsonValue
  public String getScopeName() {
    return scopeName;
  }

  /**
   * 이 scope의 데이터가 파라미터로 전달된 platform에서 사용될 수 있는지 확인한다.
   * COMMON은 모든 플랫폼에서 사용 가능하며, ANDROID/IOS는 동일한 플랫폼에서만 사용 가능하다.
   *
   * @param platform 대상 플랫폼
   * @return 사용 가능 여부
   */
  public boolean supports(Platform platform) {
    if (this == COMMON) {
      return true;
    }
    return this.name().equals(platform.name());
  }

  public static PlatformScope fromString(String platformScope) {
    return Arrays.stream(values())
        .filter(scope -> scope.name().equalsIgnoreCase(platformScope)
            || scope.scopeName.equalsIgnoreCase(platformScope))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid platform scope: " + platformScope));
  }
}
