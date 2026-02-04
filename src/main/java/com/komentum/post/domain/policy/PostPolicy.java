package com.komentum.post.domain.policy;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostPolicy {

  private final OwnerAdminPolicy ownerAdminPolicy;

  public boolean canUpdate(User postOwner) {
    return ownerAdminPolicy.validate(postOwner);
  }

  public boolean canDelete(User postOwner) {
    return ownerAdminPolicy.validate(postOwner);
  }
}
