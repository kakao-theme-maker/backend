package com.komentum.post.domain.policy;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreferPolicy {

  private final OwnerAdminPolicy ownerAdminPolicy;

  public boolean canDelete(User preferOwner) {
    return ownerAdminPolicy.validate(preferOwner);
  }
}
