package com.komentum.theme.component.domain.policy;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DesignComponentPolicy {
  private final OwnerAdminPolicy ownerAdminPolicy;

  public boolean canUpdate(User designComponentOwner) {
    return ownerAdminPolicy.validate(designComponentOwner);
  }

  public boolean canDelete(User designComponentOwner) {
    return ownerAdminPolicy.validate(designComponentOwner);
  }

}

