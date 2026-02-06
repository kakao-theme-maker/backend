package com.komentum.global.domain.policy;

import com.komentum.user.domain.User;

public interface OwnerAdminPolicy {

  boolean validate(User resourceOwner);
}
