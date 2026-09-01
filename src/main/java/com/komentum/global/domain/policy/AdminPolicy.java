package com.komentum.global.domain.policy;

import com.komentum.global.security.CurrentPrincipalProvider;
import com.komentum.global.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminPolicy {

  private final CurrentPrincipalProvider principalProvider;

  /**
   * 현재 스레드에 저장된 사용자가 Admin 권한을 갖는지 확인한다.
   * */
  public boolean validate() {
    return principalProvider.retrievePrincipal()
        .map(userDetails -> userDetails.getUserRole().equals(UserRole.ADMIN))
        .orElse(false);
  }
}
