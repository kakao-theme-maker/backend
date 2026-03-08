package com.komentum.theme.component.domain.policy;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.theme.component.domain.DesignComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DesignComponentPolicy {

  private final OwnerAdminPolicy ownerAdminPolicy;

  public boolean canUpdate(DesignComponent designComponent) {
    return ownerAdminPolicy.validate(designComponent.getUser());
  }

  public boolean canDelete(DesignComponent designComponent) {
    return ownerAdminPolicy.validate(designComponent.getUser());
  }

}

