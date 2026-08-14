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
   * 인증 정보에 저장된 email과 owner email이 동일하거나, admin 권한을 갖는 사용자라면 TRUE 반환
   * */
  public boolean validate() {
    return principalProvider.retrievePrincipal()
        .map(userDetails -> userDetails.getUserRole().equals(UserRole.ADMIN))
        .orElse(false);
  }
}
