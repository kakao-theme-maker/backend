package com.komentum.post.domain.policy;

import com.komentum.global.domain.policy.OwnerAdminPolicy;
import com.komentum.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentPolicy {

  private final OwnerAdminPolicy ownerAdminPolicy;

  public boolean canUpdate(User commentOwner) {
    return ownerAdminPolicy.validate(commentOwner);
  }

  public boolean canDelete(User commentOwner) {
    return ownerAdminPolicy.validate(commentOwner);
  }
}
