package com.komentum.post.domain.policy;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryPolicy {

  private final OwnerAdminPolicy ownerAdminPolicy;

  public boolean canUpdate(User categoryOwner) {
    return ownerAdminPolicy.validate(categoryOwner);
  }

  public boolean canDelete(User categoryOwner) {
    return ownerAdminPolicy.validate(categoryOwner);
  }
}
